package com.drivelock.app

import android.content.Context
import androidx.room.Room
import com.drivelock.app.data.local.DriveLockDatabase
import com.drivelock.app.data.repository.TripRepositoryImpl
import com.drivelock.app.detection.RealDrivingDetectionEngine
import com.drivelock.app.detection.TripEndDetector
import com.drivelock.app.detection.activity.PlayServicesActivityRecognitionDataSource
import com.drivelock.app.detection.location.FusedLocationDataSource
import com.drivelock.app.domain.repository.TripRepository
import com.drivelock.app.tracking.AndroidTripTrackingController
import com.drivelock.app.tracking.CompletedTripRecorder
import com.drivelock.app.tracking.TripSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        DriveLockDatabase::class.java,
        "drivelock.db",
    ).build()

    val tripRepository: TripRepository = TripRepositoryImpl(database.tripDao())
    val activityRecognitionDataSource = PlayServicesActivityRecognitionDataSource(context.applicationContext)
    val locationDataSource = FusedLocationDataSource(context.applicationContext)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val tripSessionManager = TripSessionManager()
    val completedTripRecorder = CompletedTripRecorder(tripSessionManager, tripRepository, applicationScope)
    val tripEndDetector = TripEndDetector()
    private val tripTrackingController = AndroidTripTrackingController(context.applicationContext)
    val detectionEngine = RealDrivingDetectionEngine(
        activityRecognitionDataSource,
        locationDataSource,
        applicationScope,
        tripTrackingController = tripTrackingController,
        tripEndDetector = tripEndDetector,
    )
}
