package com.batterycalc.app.data

import android.content.Context
import com.batterycalc.app.util.BatteryHelper
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

/**
 * Charging and usage logs are separate tables with strict lifecycle:
 * - Plug in  → end usage (→ history), start charge
 * - Unplug   → end charge (→ history), start usage
 */
class SessionRepository(
    private val dischargeDao: DischargeSessionDao,
    private val chargeDao: ChargeSessionDao
) {
    private var lastPlugEventMs = 0L
    private var lastUnplugEventMs = 0L

    fun observeActiveDischarge(): Flow<DischargeSession?> = dischargeDao.observeActiveSession()

    fun observeCompletedDischarge(): Flow<List<DischargeSession>> =
        dischargeDao.observeCompletedSessions()

    fun observeActiveCharge(): Flow<ChargeSession?> = chargeDao.observeActiveSession()

    fun observeCompletedCharge(): Flow<List<ChargeSession>> = chargeDao.observeCompletedSessions()

    suspend fun onPluggedIn(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()
        if (!acceptPlugEvent(now)) return

        endActiveUsage(now, percent)
        ensureActiveCharge(now, percent)
    }

    suspend fun onUnplugged(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()
        if (!acceptUnplugEvent(now)) return

        endActiveCharge(now, percent)
        startUsageSession(now, percent)
    }

    suspend fun reconcileSessions(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()

        if (BatteryHelper.isCharging(context)) {
            endActiveUsage(now, percent)
            ensureActiveCharge(now, percent)
        } else {
            val hadActiveCharge = chargeDao.getActiveSession() != null
            endActiveCharge(now, percent)

            when {
                dischargeDao.getActiveSession() != null -> Unit
                hadActiveCharge -> startUsageSession(now, percent)
                else -> recoverMissingUsageSession()
            }
        }
    }

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

    private suspend fun ensureActiveCharge(now: Long, percent: Int) {
        val active = chargeDao.getActiveSession()
        when {
            active == null -> {
                chargeDao.insert(ChargeSession(plugTime = now, plugPercent = percent))
            }
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

    companion object {
        private const val EVENT_DEBOUNCE_MS = 2_000L
        private const val SAME_CYCLE_WINDOW_MS = 60_000L
        private const val RECOVERY_MAX_AGE_MS = 48 * 60 * 60 * 1000L
    }
}
