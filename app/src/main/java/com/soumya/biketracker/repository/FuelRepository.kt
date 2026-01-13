package com.soumya.biketracker.repository

import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.entity.FuelEntry
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val fuelDao: FuelDao) {
    fun getAllFuelEntries(): Flow<List<FuelEntry>> {
        return fuelDao.getAllFuelEntries()
    }

    suspend fun insertFuelEntry(entry: FuelEntry) {
        fuelDao.insertFuelEntry(entry)
    }

    suspend fun getLastFuelEntry(): FuelEntry? {
        return fuelDao.getLastFuelEntry()
    }

    suspend fun clearAll(){
        fuelDao.clearAll()
    }

}