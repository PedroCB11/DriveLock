package com.drivelock.app.detection.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drivelock.app.DriveLockApplication
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val source = (context.applicationContext as DriveLockApplication).container.activityRecognitionDataSource
        val result = ActivityTransitionResult.extractResult(intent) ?: return
        result.transitionEvents.forEach { event ->
            val activity = event.activityType.toDomainActivity() ?: return@forEach
            val transition = if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) TransitionType.ENTER else TransitionType.EXIT
            source.emit(ActivityTransitionSignal(activity, transition))
        }
    }
}

private fun Int.toDomainActivity(): RecognizedActivity? = when (this) {
    DetectedActivity.IN_VEHICLE -> RecognizedActivity.IN_VEHICLE
    DetectedActivity.WALKING -> RecognizedActivity.WALKING
    DetectedActivity.RUNNING -> RecognizedActivity.RUNNING
    DetectedActivity.ON_BICYCLE -> RecognizedActivity.ON_BICYCLE
    DetectedActivity.STILL -> RecognizedActivity.STILL
    else -> null
}
