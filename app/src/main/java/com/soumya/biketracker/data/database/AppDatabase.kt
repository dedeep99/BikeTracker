package com.soumya.biketracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.database.converters.FuelConverters
import com.soumya.biketracker.data.entity.FuelEntry

@Database(entities = [FuelEntry::class], version = 6, exportSchema = false)
@TypeConverters(FuelConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fuelDao(): FuelDao
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1️⃣ Remove duplicates (keep lowest id per dateTime)
        db.execSQL("""
            DELETE FROM fuel_entries
            WHERE id NOT IN (
                SELECT MIN(id)
                FROM fuel_entries
                GROUP BY dateTime
            )
        """)

        // 2️⃣ Now safely create UNIQUE index
        db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_fuel_entries_dateTime
            ON fuel_entries(dateTime)
        """)
    }
}

