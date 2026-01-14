package com.soumya.biketracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.database.converters.FuelConverters
import com.soumya.biketracker.data.entity.FuelEntry

@Database(entities = [FuelEntry::class], version = 5, exportSchema = false)
@TypeConverters(FuelConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fuelDao(): FuelDao
}

val MIGRATION_4_5 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE fuel_entries ADD COLUMN mileage REAL"
        )
    }
}


