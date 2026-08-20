package com.drivelock.app.detection

import com.drivelock.app.detection.activity.ActivityRecognitionDataSource
import com.drivelock.app.detection.activity.ActivityTransitionSignal
import com.drivelock.app.detection.activity.RecognizedActivity
import com.drivelock.app.detection.activity.TransitionType
import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RealDrivingDetectionEngineTest {
    @Test
    fun `vehicle entry is debounced before driver confirmation`() = runTest {
        val source = FakeActivitySource()
        val engine = RealDrivingDetectionEngine(source, this, DetectionConfig(1_000))

        engine.startMonitoring()
        runCurrent()
        source.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER))
        runCurrent()
        assertEquals(DriveState.MOVEMENT_DETECTED, engine.driveState.value)

        advanceTimeBy(999)
        assertEquals(DriveState.MOVEMENT_DETECTED, engine.driveState.value)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(DriveState.CONFIRMING_DRIVER, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test
    fun `vehicle exit cancels pending confirmation`() = runTest {
        val source = FakeActivitySource()
        val engine = RealDrivingDetectionEngine(source, this, DetectionConfig(1_000))

        engine.startMonitoring()
        runCurrent()
        source.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER))
        source.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.EXIT))
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(DriveState.IDLE, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test
    fun `missing permission produces degraded monitoring state`() = runTest {
        val source = FakeActivitySource(permission = false)
        val engine = RealDrivingDetectionEngine(source, this)

        engine.startMonitoring()

        assertEquals(MonitoringState.PERMISSION_REQUIRED, engine.monitoringState.value)
    }
}

private class FakeActivitySource(private val permission: Boolean = true) : ActivityRecognitionDataSource {
    val events = MutableSharedFlow<ActivityTransitionSignal>(extraBufferCapacity = 4)
    override val signals = events
    override fun hasPermission() = permission
    override fun start(onResult: (Result<Unit>) -> Unit) = onResult(Result.success(Unit))
    override fun stop() = Unit
}
