package com.batterycalc.app.ui

import com.batterycalc.app.data.ChargeSession
import com.batterycalc.app.util.BatteryHelper
import com.batterycalc.app.util.TimeFormat

data class ChargeSessionUiModel(
    val id: Long,
    val plugTime: Long,
    val plugPercent: Int,
    val endTime: Long,
    val endPercent: Int,
    val isLive: Boolean,
    val durationMs: Long,
    val percentGained: Int,
    val chargePerHour: Float
) {
    val durationLabel: String get() = BatteryHelper.formatDuration(durationMs)
    val plugTimeLabel: String get() = TimeFormat.format(plugTime)
    val endTimeLabel: String get() = if (isLive) "Now" else TimeFormat.format(endTime)
    val chargePerHourLabel: String get() = String.format("%.1f%%/hr", chargePerHour)

    companion object {
        fun fromCompleted(session: ChargeSession): ChargeSessionUiModel? {
            val unplugTime = session.unplugTime ?: return null
            val unplugPercent = session.unplugPercent ?: return null
            val durationMs = unplugTime - session.plugTime
            val percentGained = unplugPercent - session.plugPercent

            return ChargeSessionUiModel(
                id = session.id,
                plugTime = session.plugTime,
                plugPercent = session.plugPercent,
                endTime = unplugTime,
                endPercent = unplugPercent,
                isLive = false,
                durationMs = durationMs,
                percentGained = percentGained,
                chargePerHour = BatteryHelper.drainPerHour(percentGained, durationMs)
            )
        }

        fun fromActive(session: ChargeSession, currentPercent: Int, now: Long): ChargeSessionUiModel {
            val durationMs = now - session.plugTime
            val percentGained = currentPercent - session.plugPercent

            return ChargeSessionUiModel(
                id = session.id,
                plugTime = session.plugTime,
                plugPercent = session.plugPercent,
                endTime = now,
                endPercent = currentPercent,
                isLive = true,
                durationMs = durationMs,
                percentGained = percentGained,
                chargePerHour = BatteryHelper.drainPerHour(percentGained, durationMs)
            )
        }
    }
}
