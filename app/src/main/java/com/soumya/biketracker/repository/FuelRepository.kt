package com.soumya.biketracker.repository

import android.util.Log
import androidx.room.Transaction
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.entity.FuelEntry
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
        Log.d("MileageDebug", "Insert called. isFullTank=${entry.isFullTank}")

        validateOdometer(entry)

        // Partial fill → just store it
        if (!entry.isFullTank) {
            fuelDao.insertFuelEntry(entry)
            return
        }

        // Find previous full tank
        val previousFullTank =
            fuelDao.getPreviousFullTank(entry.dateTime)

//        Log.d("MileageDebug", "Full tank detected")
//
//        Log.d(
//            "MileageDebug",
//            "Previous full tank: ${previousFullTank?.odometer}"
//        )

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

//        Log.d(
//            "MileageDebug",
//            "start time=${previousFullTank.dateTime} end time=${entry.dateTime}"
//        )

        val distanceTravelled =
            entry.odometer - previousFullTank.odometer

        val mileage =
            if (distanceTravelled > 0 && totalFuelConsumed > 0)
                distanceTravelled / totalFuelConsumed
            else null

//        Log.d(
//            "MileageDebug",
//            "distance=$distanceTravelled fuel=$totalFuelConsumed mileage=$mileage"
//        )


        fuelDao.insertFuelEntry(
            entry.copy(mileage = mileage)
        )


    }

    suspend fun clearAll() {
        fuelDao.clearAll()
    }

    private suspend fun validateOdometer(entry: FuelEntry) {
        val previousEntry =
            fuelDao.getPreviousEntry(entry.dateTime)

        if (
            previousEntry != null &&
            entry.odometer < previousEntry.odometer
        ) {
            throw IllegalArgumentException(
                "Odometer cannot be less than previous entry (${previousEntry.odometer} km)"
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

//        dumpFuelTable("AFTER UPDATE")

        // 🔥 FIX #1: Recalculate the updated full tank itself
        if (newEntry.isFullTank) {
            recalculateSingleFullTank(newEntry)
        }

        // 🔥 FIX #2: Recalculate all future full tanks
        recalculateMileageFrom(newEntry.dateTime)
        throw RuntimeException("CRASH TEST")

    }

    suspend fun deleteFuelEntry(entry: FuelEntry) {
        fuelDao.deleteFuelEntry(entry)

        recalculateMileageFrom(entry.dateTime)
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

    private suspend fun recalculateSingleFullTank(entry: FuelEntry) {

        if (!entry.isFullTank) return

        val previousFullTank =
            fuelDao.getPreviousFullTank(entry.dateTime)

        // First full tank → no mileage
        if (previousFullTank == null) {
            fuelDao.updateMileage(entry.id, null)
            return
        }

        val fuelBetween =
            fuelDao.getFuelConsumedBetween(
                startTime = previousFullTank.dateTime,
                endTime = entry.dateTime
            )

        val totalFuelConsumed =
            fuelBetween + entry.quantity

        val distanceTravelled =
            entry.odometer - previousFullTank.odometer

        val mileage =
            if (distanceTravelled > 0 && totalFuelConsumed > 0)
                distanceTravelled / totalFuelConsumed
            else
                null

        fuelDao.updateMileage(
            id = entry.id,
            mileage = mileage
        )
    }




    @Transaction
    suspend fun recalculateMileageFrom(changedDateTime: Long) {

//        debug("=== Recalculate from $changedDateTime ===")

        var currentFullTank =
            fuelDao.getNextFullTank(changedDateTime)

        while (currentFullTank != null) {

//            debug("Current FULL tank: id=${currentFullTank.id}, time=${currentFullTank.dateTime}, odo=${currentFullTank.odometer}, qty=${currentFullTank.quantity}")


            val previousFullTank =
                fuelDao.getPreviousFullTank(currentFullTank.dateTime)

            // First full tank → no mileage
            if (previousFullTank == null) {

//                debug("→ No previous full tank, mileage = null")

                fuelDao.updateMileage(
                    id = currentFullTank.id,
                    mileage = null
                )

                currentFullTank =
                    fuelDao.getNextFullTank(currentFullTank.dateTime)
                continue
            }

            // Fuel consumed BETWEEN tanks
            val fuelBetween =
                fuelDao.getFuelConsumedBetween(
                    startTime = previousFullTank.dateTime,
                    endTime = currentFullTank.dateTime
                )

//            debug("Fuel between (DAO result) = $fuelBetween")

            // 🔥 IMPORTANT: include current full tank fuel
            val totalFuelConsumed =
                fuelBetween + currentFullTank.quantity

//            debug("Total fuel used (after adding current full) = $totalFuelConsumed")

            val distanceTravelled =
                currentFullTank.odometer - previousFullTank.odometer


//            debug("Distance travelled = $distanceTravelled")

            val mileage =
                if (distanceTravelled > 0 && totalFuelConsumed > 0)
                    distanceTravelled / totalFuelConsumed
                else
                    null

//            debug("Calculated mileage = $mileage")

            fuelDao.updateMileage(
                id = currentFullTank.id,
                mileage = mileage
            )

            // Move forward
            currentFullTank =
                fuelDao.getNextFullTank(currentFullTank.dateTime)

            val entriesBetween =
                fuelDao.debugEntriesBetween(
                    previousFullTank.dateTime,
                    currentFullTank?.dateTime ?: previousFullTank.dateTime
                )

//            entriesBetween.forEach {
//                debug("ENTRY BETWEEN → time=${it.dateTime}, qty=${it.quantity}, full=${it.isFullTank}")
//            }

        }

    }

}