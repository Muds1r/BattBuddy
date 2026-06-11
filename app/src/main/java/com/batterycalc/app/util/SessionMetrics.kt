package com.batterycalc.app.util

import kotlin.math.max
import kotlin.math.roundToInt

object SessionMetrics {

    fun percentGained(startPercent: Int, endPercent: Int): Int =
        max(0, endPercent - startPercent)

    fun percentDropped(startPercent: Int, endPercent: Int): Int =
        max(0, startPercent - endPercent)

    /** Percent change per hour, e.g. 20 means ~20% each hour. */
    fun perHour(percentChange: Int, durationMs: Long): Float {
        if (percentChange <= 0 || durationMs <= 0L) return 0f
        val hours = durationMs / 3_600_000f
        if (hours < 1f / 60f) return 0f // ignore sessions shorter than 1 minute
        return percentChange / hours
    }

    fun formatPerHour(rate: Float): String {
        if (rate <= 0f) return "—"
        val rounded = rate.roundToInt()
        return if (kotlin.math.abs(rate - rounded) < 0.05f) {
            "${rounded}%/hr"
        } else {
            String.format("%.1f%%/hr", rate)
        }
    }

    fun durationMs(startTime: Long, endTime: Long): Long =
        max(0L, endTime - startTime)
}
