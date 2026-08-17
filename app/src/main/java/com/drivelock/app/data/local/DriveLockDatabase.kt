package com.drivelock.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TripEntity::class], version = 1, exportSchema = false)
abstract class DriveLockDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
}

