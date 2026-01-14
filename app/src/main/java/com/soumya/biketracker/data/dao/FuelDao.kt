package com.soumya.biketracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.soumya.biketracker.data.entity.FuelEntry

@Dao
interface FuelDao {

    @Insert
    suspend fun insertFuelEntry(entry:FuelEntry)

    @Query("SELECT * FROM fuel_entries ORDER BY dateTime DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries ORDER BY dateTime DESC LIMIT 1")
    suspend fun getLastFuelEntry(): FuelEntry?

    @Query("DELETE FROM fuel_entries")
    suspend fun clearAll()

    @Query("""
    SELECT * FROM fuel_entries
    WHERE isFullTank = 1
      AND dateTime < :currentDateTime
    ORDER BY dateTime DESC
    LIMIT 1
    """)
    suspend fun getPreviousFullTank(currentDateTime: Long): FuelEntry?

    @Query("""
    SELECT * FROM fuel_entries
    WHERE dateTime > :startTime
      AND dateTime <= :endTime
    """)
    suspend fun getEntriesBetween(
        startTime: Long,
        endTime: Long
    ): List<FuelEntry>

    @Query("""
    SELECT IFNULL(SUM(quantity), 0)
    FROM fuel_entries
    WHERE dateTime > :startTime
      AND dateTime <= :endTime
    """)
    suspend fun getFuelConsumedBetween(
        startTime: Long,
        endTime: Long
    ): Double


}