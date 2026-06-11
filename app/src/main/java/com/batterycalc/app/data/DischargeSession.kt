package com.batterycalc.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discharge_sessions")
data class DischargeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val unplugTime: Long,
    val unplugPercent: Int,
    val plugTime: Long? = null,
    val plugPercent: Int? = null,
    val isComplete: Boolean = false
)
