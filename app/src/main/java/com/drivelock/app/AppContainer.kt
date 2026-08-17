package com.drivelock.app

import android.content.Context
import androidx.room.Room
import com.drivelock.app.data.local.DriveLockDatabase
import com.drivelock.app.data.repository.TripRepositoryImpl
import com.drivelock.app.detection.FakeDrivingDetectionEngine
import com.drivelock.app.domain.repository.TripRepository

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        DriveLockDatabase::class.java,
        "drivelock.db",
    ).build()

    val tripRepository: TripRepository = TripRepositoryImpl(database.tripDao())
    val detectionEngine = FakeDrivingDetectionEngine()
}

