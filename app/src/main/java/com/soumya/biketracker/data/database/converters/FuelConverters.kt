package com.soumya.biketracker.data.database.converters

import androidx.room.TypeConverter
import com.soumya.biketracker.domain.FuelCompany
import com.soumya.biketracker.domain.FuelType

class FuelConverters {

    @TypeConverter
    fun fromFuelCompany(value: FuelCompany): String = value.name

    @TypeConverter
    fun toFuelCompany(value: String): FuelCompany =
        FuelCompany.valueOf(value)

    @TypeConverter
    fun fromFuelType(value: FuelType): String = value.name

    @TypeConverter
    fun toFuelType(value: String): FuelType =
        FuelType.valueOf(value)
}
