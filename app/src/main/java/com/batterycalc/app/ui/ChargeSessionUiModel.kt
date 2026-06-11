package com.batterycalc.app.ui

import com.batterycalc.app.data.ChargeSession
import com.batterycalc.app.util.BatteryHelper
import com.batterycalc.app.util.SessionMetrics
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
    val chargePerHourLabel: String get() = SessionMetrics.formatPerHour(chargePerHour)

    companion object {
        fun fromCompleted(session: ChargeSession): ChargeSessionUiModel? {
            val unplugTime = session.unplugTime ?: return null
            val unplugPercent = session.unplugPercent ?: return null
            val durationMs = SessionMetrics.durationMs(session.plugTime, unplugTime)
            val percentGained = SessionMetrics.percentGained(session.plugPercent, unplugPercent)

            return ChargeSessionUiModel(
                id = session.id,
                plugTime = session.plugTime,
                plugPercent = session.plugPercent,
                endTime = unplugTime,
                endPercent = unplugPercent,
                isLive = false,
                durationMs = durationMs,
                percentGained = percentGained,
                chargePerHour = SessionMetrics.perHour(percentGained, durationMs)
            )
        }

        fun fromActive(session: ChargeSession, currentPercent: Int, now: Long): ChargeSessionUiModel {
            val durationMs = SessionMetrics.durationMs(session.plugTime, now)
            val percentGained = SessionMetrics.percentGained(session.plugPercent, currentPercent)

            return ChargeSessionUiModel(
                id = session.id,
                plugTime = session.plugTime,
                plugPercent = session.plugPercent,
                endTime = now,
                endPercent = currentPercent,
                isLive = true,
                durationMs = durationMs,
                percentGained = percentGained,
                chargePerHour = SessionMetrics.perHour(percentGained, durationMs)
            )
        }
    }
}
