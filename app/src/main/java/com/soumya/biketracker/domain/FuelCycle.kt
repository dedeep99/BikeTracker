package com.soumya.biketracker.domain

import com.soumya.biketracker.data.entity.FuelEntry

data class FuelCycle(
    val previousFull: FuelEntry?,
    val currentFull: FuelEntry,
    val fuelConsumed: Double,
    val distance: Double,
    val mileage: Double?
)
