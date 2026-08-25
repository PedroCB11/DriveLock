package com.drivelock.app.detection.activity

import kotlinx.coroutines.flow.Flow

enum class RecognizedActivity { IN_VEHICLE, WALKING, RUNNING, ON_BICYCLE, STILL }
enum class TransitionType { ENTER, EXIT }
data class ActivityTransitionSignal(
    val activity: RecognizedActivity,
    val transition: TransitionType,
    val elapsedRealtimeMillis: Long = 0,
)

interface ActivityRecognitionDataSource {
    val signals: Flow<ActivityTransitionSignal>
    fun hasPermission(): Boolean
    fun start(onResult: (Result<Unit>) -> Unit)
    fun stop()
}
