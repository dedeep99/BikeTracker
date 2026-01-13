package com.soumya.biketracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Long,
    val odometer: Int,
    val quantity: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val isFullTank: Boolean,
    val fuelType: String,
    val notes: String,
    val fuelCompany: String?

)
