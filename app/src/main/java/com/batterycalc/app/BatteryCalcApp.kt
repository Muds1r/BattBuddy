package com.batterycalc.app

import android.app.Application
import com.batterycalc.app.data.AppDatabase
import com.batterycalc.app.data.SessionRepository

class BatteryCalcApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        SessionRepository(
            dischargeDao = database.dischargeSessionDao(),
            chargeDao = database.chargeSessionDao()
        )
    }
}
