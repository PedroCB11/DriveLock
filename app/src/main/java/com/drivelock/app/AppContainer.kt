package com.drivelock.app

import android.content.Context
import androidx.room.Room
import com.drivelock.app.data.local.DriveLockDatabase
import com.drivelock.app.data.local.MIGRATION_1_2
import com.drivelock.app.data.repository.TripRepositoryImpl
import com.drivelock.app.detection.RealDrivingDetectionEngine
import com.drivelock.app.detection.AndroidDrivingMonitorController
import com.drivelock.app.detection.location.FusedLocationDataSource
import com.drivelock.app.domain.repository.TripRepository
import com.drivelock.app.tracking.AndroidTripTrackingController
import com.drivelock.app.tracking.CompletedTripRecorder
import com.drivelock.app.tracking.TripSessionManager
import com.drivelock.app.ui.onboarding.OnboardingPreferences
import com.drivelock.app.ui.settings.SettingsPreferences
import com.drivelock.app.notifications.NotificationControlPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    val onboardingPreferences = OnboardingPreferences(context.applicationContext)
    val settingsPreferences = SettingsPreferences(context.applicationContext)
    val notificationControlPreferences = NotificationControlPreferences(context.applicationContext)
    private val database = Room.databaseBuilder(
        context.applicationContext,
        DriveLockDatabase::class.java,
        "drivelock.db",
    ).addMigrations(MIGRATION_1_2).build()

    val tripRepository: TripRepository = TripRepositoryImpl(database.tripDao())
    val locationDataSource = FusedLocationDataSource(context.applicationContext)
    val monitoringLocationDataSource = FusedLocationDataSource(context.applicationContext)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val tripSessionManager = TripSessionManager()
    val completedTripRecorder = CompletedTripRecorder(tripSessionManager, tripRepository, applicationScope)
    private val tripTrackingController = AndroidTripTrackingController(context.applicationContext)
    val detectionEngine = RealDrivingDetectionEngine(
        monitoringLocationDataSource,
        applicationScope,
        monitorController = AndroidDrivingMonitorController(context.applicationContext),
        tripTrackingController = tripTrackingController,
    )
}
