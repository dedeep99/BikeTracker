package com.soumya.biketracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.soumya.biketracker.domain.*

@Entity(tableName = "fuel_entries",
        indices = [
            Index(value = ["dateTime"], unique = true)
])
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Long,
    val odometer: Double,
    val quantity: Double,
    val pricePerLitre: Double,
    val totalCost: Double,
    val isFullTank: Boolean,
    val fuelCompany: FuelCompany,
    val fuelType: FuelType,
    val notes: String?,
    val mileage: Double? = null

)
