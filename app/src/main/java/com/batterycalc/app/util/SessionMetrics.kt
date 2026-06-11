package com.batterycalc.app.util

import kotlin.math.max

object SessionMetrics {

    fun percentGained(startPercent: Int, endPercent: Int): Int =
        max(0, endPercent - startPercent)

    fun percentDropped(startPercent: Int, endPercent: Int): Int =
        max(0, startPercent - endPercent)

    fun perHour(percentChange: Int, durationMs: Long): Float {
        if (percentChange <= 0 || durationMs <= 0L) return 0f
        val hours = durationMs / 3_600_000f
        return if (hours < 0.01f) 0f else percentChange / hours
    }

    fun durationMs(startTime: Long, endTime: Long): Long =
        max(0L, endTime - startTime)
}
