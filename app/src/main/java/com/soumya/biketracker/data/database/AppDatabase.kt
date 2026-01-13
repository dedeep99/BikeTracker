package com.soumya.biketracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.entity.FuelEntry

@Database(entities = [FuelEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fuelDao(): FuelDao
}