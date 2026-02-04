package com.soumya.biketracker.repository

import android.util.Log
import androidx.room.Transaction
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.domain.FuelCycle
import kotlinx.coroutines.flow.Flow

private const val DEBUG_MILEAGE = false

class FuelRepository(private val fuelDao: FuelDao) {


    private fun debug(msg: String) {
        Log.d("MileageDebug", msg)
    }

    fun getAllFuelEntries(): Flow<List<FuelEntry>> {
        return fuelDao.getAllFuelEntries()
    }

    suspend fun insertFuelEntry(entry: FuelEntry) {
        validateOdometer(entry)

        try {
            fuelDao.insertFuelEntry(entry)
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            throw IllegalArgumentException(
                "An entry already exists for this date and time"
            )
        }

        recalculateMileageFrom(entry)
    }

    suspend fun clearAll() {
        fuelDao.clearAll()
    }

    private suspend fun buildFuelCycle(
        currentFull: FuelEntry
    ): FuelCycle {

        require(currentFull.isFullTank)

        val previousFull =
            fuelDao.getPreviousFullTank(currentFull.dateTime)
                ?: return FuelCycle(
                    previousFull = null,
                    currentFull = currentFull,
                    fuelConsumed = 0.0,
                    distance = 0.0,
                    mileage = null
                )

        val fuelBetween =
            fuelDao.getFuelConsumedBetween(
                startTime = previousFull.dateTime,
                endTime = currentFull.dateTime
            )

        val totalFuel = fuelBetween + currentFull.quantity
        val distance = currentFull.odometer - previousFull.odometer

        val mileage =
            if (distance > 0 && totalFuel > 0)
                distance / totalFuel
            else null

        return FuelCycle(
            previousFull = previousFull,
            currentFull = currentFull,
            fuelConsumed = totalFuel,
            distance = distance,
            mileage = mileage
        )
    }


    private suspend fun validateOdometer(entry: FuelEntry) {

        val before = fuelDao.getEntryBefore(entry.dateTime)
            ?.takeIf { it.id != entry.id }

        val after = fuelDao.getEntryAfter(entry.dateTime)
            ?.takeIf { it.id != entry.id }

        if (before != null && entry.odometer <= before.odometer) {
            throw IllegalArgumentException(
                "Odometer cannot be less than or equal to previous entry (${before.odometer} km)"
            )
        }

        if (after != null && entry.odometer >= after.odometer) {
            throw IllegalArgumentException(
                "Odometer cannot be greater than or equal to next entry (${after.odometer} km)"
            )
        }

        val sameTimeEntry =
            fuelDao.getEntryAtTime(entry.dateTime)
                ?.takeIf { it.id != entry.id }

        if (sameTimeEntry != null) {
            throw IllegalArgumentException(
                "An entry already exists at this date and time"
            )
        }
    }

    @Transaction
    suspend fun updateAndRecalculateMileage(
        oldEntry: FuelEntry,
        newEntry: FuelEntry
    ) {
        validateOdometer(newEntry)
        fuelDao.updateFuelEntry(newEntry)

        recalculateMileageFrom(newEntry)
    }

    suspend fun deleteFuelEntry(entry: FuelEntry) {
        fuelDao.deleteFuelEntry(entry)
        recalculateMileageFrom(entry)
    }

    suspend fun dumpFuelTable(tag: String) {
        if (!DEBUG_MILEAGE) return

        val all = fuelDao.getAllOnce()
        println("==== FUEL TABLE [$tag] ====")
        all.forEach {
            println(
                "id=${it.id}, time=${it.dateTime}, odo=${it.odometer}, " +
                        "qty=${it.quantity}, full=${it.isFullTank}, mileage=${it.mileage}"
            )
        }
        println("===========================")
    }


    @Transaction
    suspend fun recalculateMileageFrom(
        changedEntry: FuelEntry
    ) {
        // 🔥 ALWAYS start from the changed full tank itself
        var currentFullTank =
            if (changedEntry.isFullTank) {
                // Re-fetch from DB to get correct ID/state
                fuelDao.getPreviousFullTank(changedEntry.dateTime)
                    ?.let { fuelDao.getNextFullTank(it.dateTime) }
                    ?: fuelDao.getNextFullTank(changedEntry.dateTime)
            } else {
                val previousFull =
                    fuelDao.getPreviousFullTank(changedEntry.dateTime)

                if (previousFull != null)
                    fuelDao.getNextFullTank(previousFull.dateTime)
                else
                    fuelDao.getNextFullTank(changedEntry.dateTime)
            }

        while (currentFullTank != null) {
            val cycle = buildFuelCycle(currentFullTank)

            fuelDao.updateMileage(
                id = currentFullTank.id,
                mileage = cycle.mileage
            )

            currentFullTank =
                fuelDao.getNextFullTank(currentFullTank.dateTime)
        }
    }

}