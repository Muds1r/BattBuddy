package com.batterycalc.app.data

import android.content.Context
import com.batterycalc.app.util.BatteryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

/**
 * Charging and usage logs are separate:
 * - Plug in  → end usage (history), start charge
 * - Unplug   → end charge (history), start usage
 *
 * State is tracked in [BatteryStatePrefs] so background checks catch missed broadcasts.
 */
class SessionRepository(
    private val dischargeDao: DischargeSessionDao,
    private val chargeDao: ChargeSessionDao,
    private val batteryStatePrefs: BatteryStatePrefs
) {
    private var lastPlugEventMs = 0L
    private var lastUnplugEventMs = 0L

    fun observeActiveDischarge(): Flow<DischargeSession?> = dischargeDao.observeActiveSession()

    fun observeCompletedDischarge(): Flow<List<DischargeSession>> =
        dischargeDao.observeCompletedSessions()

    fun observeActiveCharge(): Flow<ChargeSession?> = chargeDao.observeActiveSession()

    fun observeCompletedCharge(): Flow<List<ChargeSession>> = chargeDao.observeCompletedSessions()

    suspend fun hasActiveSession(): Boolean =
        chargeDao.getActiveSession() != null || dischargeDao.getActiveSession() != null

    suspend fun onPluggedIn(context: Context) {
        val percent = readBatteryPercent(context) ?: return
        val now = System.currentTimeMillis()
        if (!acceptPlugEvent(now)) return

        endActiveUsage(now, percent)
        ensureActiveCharge(now, percent)
        batteryStatePrefs.setWasCharging(true)
    }

    suspend fun onUnplugged(context: Context) {
        // Brief delay so battery % can update after cable removal
        delay(UNPLUG_READ_DELAY_MS)
        val percent = readBatteryPercent(context) ?: return
        val now = System.currentTimeMillis()
        if (!acceptUnplugEvent(now)) return

        endActiveCharge(now, percent)
        startUsageSession(now, percent)
        batteryStatePrefs.setWasCharging(false)
    }

    /**
     * Detects charging ↔ battery transitions without opening the app.
     * Safe to call from UI refresh, boot, and background worker.
     */
    suspend fun syncChargingState(context: Context) {
        val percent = readBatteryPercent(context) ?: return
        val now = System.currentTimeMillis()
        val charging = BatteryHelper.isCharging(context)

        if (!batteryStatePrefs.isInitialized()) {
            batteryStatePrefs.setWasCharging(charging)
            fixInconsistentSessions(charging, percent, now)
            return
        }

        val wasCharging = batteryStatePrefs.getWasCharging(charging)
        if (charging != wasCharging) {
            if (charging) {
                onPluggedIn(context)
            } else {
                onUnplugged(context)
            }
            return
        }

        fixInconsistentSessions(charging, percent, now)
    }

    /** @deprecated Use [syncChargingState] */
    suspend fun reconcileSessions(context: Context) = syncChargingState(context)

    private fun acceptPlugEvent(now: Long): Boolean {
        if (now - lastPlugEventMs < EVENT_DEBOUNCE_MS) return false
        lastPlugEventMs = now
        return true
    }

    private fun acceptUnplugEvent(now: Long): Boolean {
        if (now - lastUnplugEventMs < EVENT_DEBOUNCE_MS) return false
        lastUnplugEventMs = now
        return true
    }

    private suspend fun fixInconsistentSessions(charging: Boolean, percent: Int, now: Long) {
        if (charging) {
            dischargeDao.getActiveSession()?.let { endActiveUsage(now, percent) }
            ensureActiveCharge(now, percent)
        } else {
            val orphanCharge = chargeDao.getActiveSession()
            if (orphanCharge != null) {
                val age = now - orphanCharge.plugTime
                if (age <= STALE_ORPHAN_MAX_MS) {
                    endActiveCharge(now, percent)
                    startUsageSession(now, percent)
                } else {
                    chargeDao.deleteById(orphanCharge.id)
                    if (dischargeDao.getActiveSession() == null) {
                        startUsageSession(now, percent)
                    }
                }
            } else if (dischargeDao.getActiveSession() == null) {
                recoverMissingUsageSession()
            }
        }
    }

    private suspend fun ensureActiveCharge(now: Long, percent: Int) {
        val active = chargeDao.getActiveSession()
        when {
            active == null -> chargeDao.insert(ChargeSession(plugTime = now, plugPercent = percent))
            isSameChargeCycle(active, now, percent) -> Unit
            else -> {
                endActiveCharge(now, percent)
                chargeDao.insert(ChargeSession(plugTime = now, plugPercent = percent))
            }
        }
    }

    private fun isSameChargeCycle(session: ChargeSession, now: Long, percent: Int): Boolean {
        val recent = now - session.plugTime < SAME_CYCLE_WINDOW_MS
        val similarPercent = abs(session.plugPercent - percent) <= 1
        return recent && similarPercent
    }

    private suspend fun endActiveUsage(endTime: Long, endPercent: Int) {
        dischargeDao.getActiveSession()?.let { active ->
            dischargeDao.update(
                active.copy(
                    plugTime = endTime,
                    plugPercent = endPercent,
                    isComplete = true
                )
            )
        }
    }

    private suspend fun endActiveCharge(endTime: Long, endPercent: Int) {
        chargeDao.getActiveSession()?.let { active ->
            chargeDao.update(
                active.copy(
                    unplugTime = endTime,
                    unplugPercent = endPercent,
                    isComplete = true
                )
            )
        }
    }

    private suspend fun startUsageSession(unplugTime: Long, unplugPercent: Int) {
        if (dischargeDao.getActiveSession() != null) return
        dischargeDao.insert(
            DischargeSession(
                unplugTime = unplugTime,
                unplugPercent = unplugPercent
            )
        )
    }

    private suspend fun recoverMissingUsageSession() {
        val lastCharge = chargeDao.getLatestCompleted() ?: return
        val unplugTime = lastCharge.unplugTime ?: return
        val unplugPercent = lastCharge.unplugPercent ?: return

        if (dischargeDao.hasSessionNearUnplug(unplugTime) > 0) return
        if (System.currentTimeMillis() - unplugTime > RECOVERY_MAX_AGE_MS) return

        dischargeDao.insert(
            DischargeSession(
                unplugTime = unplugTime,
                unplugPercent = unplugPercent
            )
        )
    }

    private suspend fun readBatteryPercent(context: Context): Int? {
        val percent = BatteryHelper.getBatteryPercent(context)
        return percent.takeIf { it >= 0 }
    }

    companion object {
        private const val EVENT_DEBOUNCE_MS = 2_000L
        private const val UNPLUG_READ_DELAY_MS = 800L
        private const val SAME_CYCLE_WINDOW_MS = 60_000L
        private const val STALE_ORPHAN_MAX_MS = 6 * 60 * 60 * 1000L
        private const val RECOVERY_MAX_AGE_MS = 48 * 60 * 60 * 1000L
    }
}
