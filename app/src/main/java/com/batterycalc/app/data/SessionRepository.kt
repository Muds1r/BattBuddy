package com.batterycalc.app.data

import android.content.Context
import com.batterycalc.app.util.BatteryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

/**
 * Logs are created only on plug/unplug events (broadcast or detected state change).
 * Opening the app never creates sessions.
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
        delay(UNPLUG_READ_DELAY_MS)
        val percent = readBatteryPercent(context) ?: return
        val now = System.currentTimeMillis()
        if (!acceptUnplugEvent(now)) return

        endActiveCharge(now, percent)
        startUsageSession(now, percent)
        batteryStatePrefs.setWasCharging(false)
    }

    /**
     * Background/boot only: detect charging state change vs last known state.
     * Does not create logs unless state actually changed.
     */
    suspend fun checkChargingTransition(context: Context) {
        val charging = BatteryHelper.isCharging(context)

        if (!batteryStatePrefs.isInitialized()) {
            batteryStatePrefs.setWasCharging(charging)
            return
        }

        val wasCharging = batteryStatePrefs.getWasCharging(charging)
        if (charging == wasCharging) return

        if (charging) {
            onPluggedIn(context)
        } else {
            onUnplugged(context)
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

    private suspend fun readBatteryPercent(context: Context): Int? {
        val percent = BatteryHelper.getBatteryPercent(context)
        return percent.takeIf { it >= 0 }
    }

    companion object {
        private const val EVENT_DEBOUNCE_MS = 2_000L
        private const val UNPLUG_READ_DELAY_MS = 800L
        private const val SAME_CYCLE_WINDOW_MS = 60_000L
    }
}
