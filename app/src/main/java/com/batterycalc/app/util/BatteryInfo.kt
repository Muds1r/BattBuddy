package com.batterycalc.app.util

data class BatteryInfo(
    val levelPercent: Int,
    val voltageVolts: Float?,
    val currentMa: Int?,
    val powerWatts: Float?,
    val healthStatus: String,
    val capacityPercent: Int?,
    val temperatureC: Float?,
    val technology: String?,
    val cycleCountAvailable: Boolean
) {
    val voltageLabel: String
        get() = voltageVolts?.let { String.format("%.2f V", it) } ?: "—"

    val currentLabel: String
        get() = currentMa?.let {
            val sign = if (it > 0) "+" else ""
            "$sign${it} mA"
        } ?: "—"

    val powerLabel: String
        get() = powerWatts?.let { String.format("%.1f W", it) } ?: "—"

    val capacityLabel: String
        get() = capacityPercent?.let { "$it% of design" } ?: "—"

    val temperatureLabel: String
        get() = temperatureC?.let { String.format("%.1f °C", it) } ?: "—"

    val cycleCountLabel: String
        get() = if (cycleCountAvailable) "See device settings" else "Not available to apps"
}
