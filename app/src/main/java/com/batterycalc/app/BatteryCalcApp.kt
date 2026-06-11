package com.batterycalc.app

import android.app.Application
import com.batterycalc.app.data.AppDatabase
import com.batterycalc.app.data.BatteryStatePrefs
import com.batterycalc.app.data.SessionRepository
import com.batterycalc.app.work.BatteryMonitorWorker

class BatteryCalcApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val batteryStatePrefs by lazy { BatteryStatePrefs(this) }
    val repository by lazy {
        SessionRepository(
            dischargeDao = database.dischargeSessionDao(),
            chargeDao = database.chargeSessionDao(),
            batteryStatePrefs = batteryStatePrefs
        )
    }

    override fun onCreate() {
        super.onCreate()
        BatteryMonitorWorker.schedule(this)
    }
}
