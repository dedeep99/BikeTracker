package com.soumya.biketracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Long,
    val odometer: Double,
    val quantity: Double,
    val pricePerLitre: Double,
    val totalCost: Double,
    val isFullTank: Boolean,
    val fuelType: String,
    val fuelCompany: String,
    val notes: String?

)
