package com.batterycalc.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charge_sessions")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plugTime: Long,
    val plugPercent: Int,
    val unplugTime: Long? = null,
    val unplugPercent: Int? = null,
    val isComplete: Boolean = false
)
