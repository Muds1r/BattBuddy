package com.batterycalc.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DischargeSessionDao {

    @Insert
    suspend fun insert(session: DischargeSession): Long

    @Update
    suspend fun update(session: DischargeSession)

    @Query("SELECT * FROM discharge_sessions WHERE isComplete = 0 ORDER BY unplugTime DESC LIMIT 1")
    suspend fun getActiveSession(): DischargeSession?

    @Query("SELECT * FROM discharge_sessions WHERE isComplete = 0 ORDER BY unplugTime DESC LIMIT 1")
    fun observeActiveSession(): Flow<DischargeSession?>

    @Query("SELECT * FROM discharge_sessions WHERE isComplete = 1 ORDER BY unplugTime DESC")
    fun observeCompletedSessions(): Flow<List<DischargeSession>>
}
