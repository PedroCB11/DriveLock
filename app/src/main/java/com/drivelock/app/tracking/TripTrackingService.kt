package com.drivelock.app.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.drivelock.app.DriveLockApplication
import com.drivelock.app.MainActivity
import com.drivelock.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TripTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val container get() = (application as DriveLockApplication).container
    private var trackingJob: Job? = null
    private var tickerJob: Job? = null
    private var notificationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopTracking() else startTracking()
        return START_NOT_STICKY
    }

    private fun startTracking() {
        if (trackingJob != null) return
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(TripSessionState()),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
        container.tripSessionManager.start(System.currentTimeMillis(), SystemClock.elapsedRealtime())
        trackingJob = serviceScope.launch {
            container.locationDataSource.samples.collectLatest(container.tripSessionManager::addLocation)
        }
        tickerJob = serviceScope.launch {
            while (true) {
                container.tripSessionManager.updateElapsed(SystemClock.elapsedRealtime())
                delay(1_000)
            }
        }
        notificationJob = serviceScope.launch {
            container.tripSessionManager.state.collectLatest { state ->
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(state))
            }
        }
        container.locationDataSource.start { result -> if (result.isFailure) stopTracking() }
    }

    private fun stopTracking() {
        container.locationDataSource.stop()
        trackingJob?.cancel(); trackingJob = null
        tickerJob?.cancel(); tickerJob = null
        notificationJob?.cancel(); notificationJob = null
        container.tripSessionManager.end()
        container.detectionEngine.onTrackingStopped()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        container.locationDataSource.stop()
        serviceScope.cancel()
        if (container.tripSessionManager.state.value.isActive) {
            container.tripSessionManager.end()
            container.detectionEngine.onTrackingStopped()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.trip_notification_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun buildNotification(state: TripSessionState): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopTrip = PendingIntent.getService(
            this, 1, Intent(this, TripTrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(getString(R.string.trip_notification_title))
            .setContentText(getString(R.string.trip_notification_text, state.elapsedMillis / 60_000, state.distanceMeters / 1_000))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, getString(R.string.end_trip), stopTrip)
            .build()
    }

    companion object {
        const val ACTION_START = "com.drivelock.app.action.START_TRIP"
        const val ACTION_STOP = "com.drivelock.app.action.STOP_TRIP"
        private const val CHANNEL_ID = "active_trip"
        private const val NOTIFICATION_ID = 1001
    }
}
