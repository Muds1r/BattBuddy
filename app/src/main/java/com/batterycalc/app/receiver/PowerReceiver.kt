package com.batterycalc.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.batterycalc.app.BatteryCalcApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PowerReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == null) return

        val app = context.applicationContext as BatteryCalcApp
        val repository = app.repository

        val pendingResult = goAsync()

        scope.launch {
            try {
                when (intent.action) {
                    Intent.ACTION_POWER_DISCONNECTED -> repository.onUnplugged(context)
                    Intent.ACTION_POWER_CONNECTED -> repository.onPluggedIn(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
