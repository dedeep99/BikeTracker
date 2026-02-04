package com.soumya.biketracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.soumya.biketracker.data.entity.FuelEntry

@Dao
interface FuelDao {

    @Insert
    suspend fun insertFuelEntry(entry:FuelEntry)

    @Query("SELECT * FROM fuel_entries ORDER BY dateTime DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("""
    SELECT * FROM fuel_entries
    WHERE dateTime < :dateTime
    ORDER BY dateTime DESC
    LIMIT 1
    """)
    suspend fun getPreviousEntry(dateTime: Long): FuelEntry?

    @Query("SELECT * FROM fuel_entries ORDER BY dateTime")
    suspend fun getAllOnce(): List<FuelEntry>

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
      AND dateTime < :endTime
    """)
    suspend fun getFuelConsumedBetween(
        startTime: Long,
        endTime: Long
    ): Double

    @Query("""
    SELECT * FROM fuel_entries
    WHERE isFullTank = 1
      AND dateTime > :afterDateTime
    ORDER BY dateTime ASC
    LIMIT 1
    """)
    suspend fun getNextFullTank(afterDateTime: Long): FuelEntry?

    @Query("""
    UPDATE fuel_entries
    SET mileage = :mileage
    WHERE id = :id
    """)
    suspend fun updateMileage(
        id: Long,
        mileage: Double?
    )

    @Update
    suspend fun updateFuelEntry(entry: FuelEntry)

    @Delete
    suspend fun deleteFuelEntry(entry: FuelEntry)

    @Query("""
    SELECT *
    FROM fuel_entries
    WHERE dateTime >= :startTime
      AND dateTime <= :endTime
    ORDER BY dateTime
""")
    suspend fun debugEntriesBetween(
        startTime: Long,
        endTime: Long
    ): List<FuelEntry>

    @Query("""
    SELECT * FROM fuel_entries
    WHERE dateTime < :time
    ORDER BY dateTime DESC
    LIMIT 1
""")
    suspend fun getEntryBefore(time: Long): FuelEntry?

    @Query("""
    SELECT * FROM fuel_entries
    WHERE dateTime > :time
    ORDER BY dateTime ASC
    LIMIT 1
""")
    suspend fun getEntryAfter(time: Long): FuelEntry?

    @Query("""
    SELECT * FROM fuel_entries
    WHERE dateTime = :time
    LIMIT 1
""")
    suspend fun getEntryAtTime(time: Long): FuelEntry?





}