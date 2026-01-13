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

}