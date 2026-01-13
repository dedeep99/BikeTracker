package com.soumya.biketracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.database.converters.FuelConverters
import com.soumya.biketracker.data.entity.FuelEntry

@Database(entities = [FuelEntry::class], version = 4, exportSchema = false)
@TypeConverters(FuelConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fuelDao(): FuelDao
}