package com.drivelock.app.detection.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PlayServicesActivityRecognitionDataSource(private val context: Context) : ActivityRecognitionDataSource {
    private val mutableSignals = MutableSharedFlow<ActivityTransitionSignal>(extraBufferCapacity = 16)
    override val signals = mutableSignals.asSharedFlow()
    private val client = ActivityRecognition.getClient(context)

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ActivityTransitionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    override fun hasPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    override fun start(onResult: (Result<Unit>) -> Unit) {
        if (!hasPermission()) {
            onResult(Result.failure(SecurityException("Activity recognition permission is required")))
            return
        }
        val activities = listOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.STILL,
        )
        val transitions = activities.flatMap { activity ->
            listOf(ActivityTransition.ACTIVITY_TRANSITION_ENTER, ActivityTransition.ACTIVITY_TRANSITION_EXIT).map { type ->
                ActivityTransition.Builder().setActivityType(activity).setActivityTransition(type).build()
            }
        }
        client.requestActivityTransitionUpdates(ActivityTransitionRequest(transitions), pendingIntent)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun stop() {
        if (hasPermission()) client.removeActivityTransitionUpdates(pendingIntent)
    }

    internal fun emit(signal: ActivityTransitionSignal) {
        mutableSignals.tryEmit(signal)
    }
}
