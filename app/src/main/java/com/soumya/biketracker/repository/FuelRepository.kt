package com.soumya.biketracker.repository

import android.util.Log
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.entity.FuelEntry
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val fuelDao: FuelDao) {
    fun getAllFuelEntries(): Flow<List<FuelEntry>> {
        return fuelDao.getAllFuelEntries()
    }

    suspend fun insertFuelEntry(entry: FuelEntry) {
        Log.d("MileageDebug", "Insert called. isFullTank=${entry.isFullTank}")

        // Partial fill → just store it
        if (!entry.isFullTank) {
            fuelDao.insertFuelEntry(entry)
            return
        }

        // Find previous full tank
        val previousFullTank =
            fuelDao.getPreviousFullTank(entry.dateTime)

        Log.d("MileageDebug", "Full tank detected")

        Log.d(
            "MileageDebug",
            "Previous full tank: ${previousFullTank?.odometer}"
        )

        // First ever full tank → no mileage
        if (previousFullTank == null) {
            fuelDao.insertFuelEntry(entry)
            return
        }

        // Fuel consumed between tanks

        val fuelBetween =
            fuelDao.getFuelConsumedBetween(
                startTime = previousFullTank.dateTime,
                endTime = entry.dateTime
            )

        val totalFuelConsumed = fuelBetween + entry.quantity

        Log.d(
            "MileageDebug",
            "start time=${previousFullTank.dateTime} end time=${entry.dateTime}"
        )

        val distanceTravelled =
            entry.odometer - previousFullTank.odometer

        val mileage =
            if (distanceTravelled > 0 && totalFuelConsumed > 0)
                distanceTravelled / totalFuelConsumed
            else null

        Log.d(
            "MileageDebug",
            "distance=$distanceTravelled fuel=$totalFuelConsumed mileage=$mileage"
        )


        fuelDao.insertFuelEntry(
            entry.copy(mileage = mileage)
        )


    }


    suspend fun getLastFuelEntry(): FuelEntry? {
        return fuelDao.getLastFuelEntry()
    }

    suspend fun clearAll(){
        fuelDao.clearAll()
    }

}