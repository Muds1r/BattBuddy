package com.batterycalc.app.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.abs
import kotlin.math.max

object BatteryHelper {

    private fun batteryStatus(context: Context) = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    private fun batteryManager(context: Context) =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun getBatteryPercent(context: Context): Int {
        val status = batteryStatus(context) ?: return -1
        val level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return -1
        return (level * 100f / scale).toInt()
    }

    fun isCharging(context: Context): Boolean {
        val status = batteryStatus(context) ?: return false
        val chargeStatus = status.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            chargeStatus == BatteryManager.BATTERY_STATUS_FULL
    }

    fun getBatteryInfo(context: Context): BatteryInfo {
        val status = batteryStatus(context)
        val manager = batteryManager(context)
        val level = getBatteryPercent(context)

        val voltageMv = status?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }
        val voltageVolts = voltageMv?.let { it / 1000f }

        val currentUa = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            .takeIf { it != Int.MIN_VALUE && it != 0 }

        val currentMa = currentUa?.let { it / 1000 }

        val powerWatts = if (voltageVolts != null && currentMa != null) {
            abs(voltageVolts * currentMa / 1000f)
        } else null

        val health = status?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthStatus = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val capacity = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .takeIf { it in 1..100 }

        val tempTenths = status?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.takeIf { it > 0 }
        val temperatureC = tempTenths?.let { it / 10f }

        val technology = status?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        return BatteryInfo(
            levelPercent = level,
            voltageVolts = voltageVolts,
            currentMa = currentMa,
            powerWatts = powerWatts,
            healthStatus = healthStatus,
            capacityPercent = capacity,
            temperatureC = temperatureC,
            technology = technology,
            cycleCountAvailable = false
        )
    }

    fun formatDuration(durationMs: Long): String {
        val totalMinutes = max(0L, durationMs / 60_000L)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
