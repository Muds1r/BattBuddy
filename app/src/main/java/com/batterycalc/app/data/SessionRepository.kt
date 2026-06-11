package com.batterycalc.app.data

import android.content.Context
import com.batterycalc.app.util.BatteryHelper
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val dischargeDao: DischargeSessionDao,
    private val chargeDao: ChargeSessionDao
) {

    fun observeActiveDischarge(): Flow<DischargeSession?> = dischargeDao.observeActiveSession()

    fun observeCompletedDischarge(): Flow<List<DischargeSession>> =
        dischargeDao.observeCompletedSessions()

    fun observeActiveCharge(): Flow<ChargeSession?> = chargeDao.observeActiveSession()

    fun observeCompletedCharge(): Flow<List<ChargeSession>> = chargeDao.observeCompletedSessions()

    suspend fun onPluggedIn(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()

        dischargeDao.getActiveSession()?.let { active ->
            dischargeDao.update(
                active.copy(
                    plugTime = now,
                    plugPercent = percent,
                    isComplete = true
                )
            )
        }

        if (chargeDao.getActiveSession() == null) {
            chargeDao.insert(
                ChargeSession(
                    plugTime = now,
                    plugPercent = percent
                )
            )
        }
    }

    suspend fun onUnplugged(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()

        chargeDao.getActiveSession()?.let { active ->
            chargeDao.update(
                active.copy(
                    unplugTime = now,
                    unplugPercent = percent,
                    isComplete = true
                )
            )
        }

        if (dischargeDao.getActiveSession() == null) {
            dischargeDao.insert(
                DischargeSession(
                    unplugTime = now,
                    unplugPercent = percent
                )
            )
        }
    }

    suspend fun syncChargingState(context: Context) {
        val percent = BatteryHelper.getBatteryPercent(context)
        if (percent < 0) return
        val now = System.currentTimeMillis()

        if (BatteryHelper.isCharging(context)) {
            dischargeDao.getActiveSession()?.let { active ->
                dischargeDao.update(
                    active.copy(
                        plugTime = now,
                        plugPercent = percent,
                        isComplete = true
                    )
                )
            }
            if (chargeDao.getActiveSession() == null) {
                chargeDao.insert(
                    ChargeSession(plugTime = now, plugPercent = percent)
                )
            }
        } else {
            chargeDao.getActiveSession()?.let { active ->
                chargeDao.update(
                    active.copy(
                        unplugTime = now,
                        unplugPercent = percent,
                        isComplete = true
                    )
                )
            }
        }
    }
}
