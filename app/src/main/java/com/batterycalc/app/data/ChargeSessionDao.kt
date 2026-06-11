package com.batterycalc.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeSessionDao {

    @Insert
    suspend fun insert(session: ChargeSession): Long

    @Update
    suspend fun update(session: ChargeSession)

    @Query("SELECT * FROM charge_sessions WHERE isComplete = 0 ORDER BY plugTime DESC LIMIT 1")
    suspend fun getActiveSession(): ChargeSession?

    @Query("SELECT * FROM charge_sessions WHERE isComplete = 0 ORDER BY plugTime DESC LIMIT 1")
    fun observeActiveSession(): Flow<ChargeSession?>

    @Query("SELECT * FROM charge_sessions WHERE isComplete = 1 ORDER BY unplugTime DESC")
    fun observeCompletedSessions(): Flow<List<ChargeSession>>

    @Query("SELECT * FROM charge_sessions WHERE isComplete = 1 ORDER BY unplugTime DESC LIMIT 1")
    suspend fun getLatestCompleted(): ChargeSession?
}
