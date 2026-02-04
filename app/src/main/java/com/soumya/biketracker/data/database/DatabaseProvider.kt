package com.soumya.biketracker.data.database

import android.content.Context
import androidx.room.Room


object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "bike_tracker_db"
                    ).addMigrations(MIGRATION_5_6).build()
            INSTANCE = instance
            instance

        }
    }
}