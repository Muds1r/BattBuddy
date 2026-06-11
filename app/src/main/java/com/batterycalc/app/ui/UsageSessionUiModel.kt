package com.batterycalc.app.ui

import com.batterycalc.app.data.DischargeSession
import com.batterycalc.app.util.BatteryHelper
import com.batterycalc.app.util.SessionMetrics
import com.batterycalc.app.util.TimeFormat

data class UsageSessionUiModel(
    val id: Long,
    val unplugTime: Long,
    val unplugPercent: Int,
    val endTime: Long,
    val endPercent: Int,
    val isLive: Boolean,
    val durationMs: Long,
    val percentDrop: Int,
    val drainPerHour: Float
) {
    val durationLabel: String get() = BatteryHelper.formatDuration(durationMs)
    val unplugTimeLabel: String get() = TimeFormat.format(unplugTime)
    val endTimeLabel: String get() = if (isLive) "Now" else TimeFormat.format(endTime)
    val drainPerHourLabel: String get() = SessionMetrics.formatPerHour(drainPerHour)

    companion object {
        fun fromCompleted(session: DischargeSession): UsageSessionUiModel? {
            val plugTime = session.plugTime ?: return null
            val plugPercent = session.plugPercent ?: return null
            val durationMs = SessionMetrics.durationMs(session.unplugTime, plugTime)
            val percentDrop = SessionMetrics.percentDropped(session.unplugPercent, plugPercent)

            return UsageSessionUiModel(
                id = session.id,
                unplugTime = session.unplugTime,
                unplugPercent = session.unplugPercent,
                endTime = plugTime,
                endPercent = plugPercent,
                isLive = false,
                durationMs = durationMs,
                percentDrop = percentDrop,
                drainPerHour = SessionMetrics.perHour(percentDrop, durationMs)
            )
        }

        fun fromActive(session: DischargeSession, currentPercent: Int, now: Long): UsageSessionUiModel {
            val durationMs = SessionMetrics.durationMs(session.unplugTime, now)
            val percentDrop = SessionMetrics.percentDropped(session.unplugPercent, currentPercent)

            return UsageSessionUiModel(
                id = session.id,
                unplugTime = session.unplugTime,
                unplugPercent = session.unplugPercent,
                endTime = now,
                endPercent = currentPercent,
                isLive = true,
                durationMs = durationMs,
                percentDrop = percentDrop,
                drainPerHour = SessionMetrics.perHour(percentDrop, durationMs)
            )
        }
    }
}
