package com.batterycalc.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormat {
    private val dateTime = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    fun format(timestamp: Long): String = dateTime.format(Date(timestamp))
}
