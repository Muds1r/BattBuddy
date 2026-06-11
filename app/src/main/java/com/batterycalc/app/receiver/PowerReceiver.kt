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
        val action = intent?.action ?: return
        val repository = (context.applicationContext as BatteryCalcApp).repository
        val pendingResult = goAsync()

        scope.launch {
            try {
                when (action) {
                    in PLUG_ACTIONS -> repository.onPluggedIn(context)
                    in UNPLUG_ACTIONS -> repository.onUnplugged(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val PLUG_ACTIONS = setOf(
            Intent.ACTION_POWER_CONNECTED,
            "android.intent.action.POWER_CONNECTED"
        )
        private val UNPLUG_ACTIONS = setOf(
            Intent.ACTION_POWER_DISCONNECTED,
            "android.intent.action.POWER_DISCONNECTED"
        )
    }
}
