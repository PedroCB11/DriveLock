package com.drivelock.app.detection

import com.drivelock.app.detection.activity.ActivityRecognitionDataSource
import com.drivelock.app.detection.activity.ActivityTransitionSignal
import com.drivelock.app.detection.activity.RecognizedActivity
import com.drivelock.app.detection.activity.TransitionType
import com.drivelock.app.detection.location.LocationDataSource
import com.drivelock.app.detection.location.LocationSample
import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RealDrivingDetectionEngineTest {
    private val config = DetectionConfig(5f, 1_000, 3, 30f)

    @Test fun `vehicle entry requires sustained valid speed before confirmation`() = runTest {
        val activity = FakeActivitySource()
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(activity, location, this, config)
        engine.startMonitoring(); runCurrent()
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER)); runCurrent()
        assertEquals(DriveState.MOVEMENT_DETECTED, engine.driveState.value)
        location.events.emit(sample(6f, 1_000)); location.events.emit(sample(7f, 1_500)); location.events.emit(sample(8f, 2_000)); runCurrent()
        assertEquals(DriveState.CONFIRMING_DRIVER, engine.driveState.value)
        engine.confirmDriver()
        assertEquals(DriverDecision.DRIVER, engine.driverDecision.value)
        assertEquals(DriveState.DRIVING, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test fun `inaccurate location resets sustained speed window`() = runTest {
        val activity = FakeActivitySource()
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(activity, location, this, config)
        engine.startMonitoring(); runCurrent()
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER)); runCurrent()
        location.events.emit(sample(8f, 1_000)); location.events.emit(sample(8f, 1_500, 100f)); location.events.emit(sample(8f, 2_000)); location.events.emit(sample(8f, 2_500)); runCurrent()
        assertEquals(DriveState.MOVEMENT_DETECTED, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test fun `vehicle exit cancels location verification`() = runTest {
        val activity = FakeActivitySource()
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(activity, location, this, config)
        engine.startMonitoring(); runCurrent()
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER))
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.EXIT)); runCurrent()
        assertEquals(DriveState.IDLE, engine.driveState.value)
        assertEquals(1, location.stopCount)
        engine.stopMonitoring()
    }

    @Test fun `missing activity permission produces degraded monitoring state`() = runTest {
        val engine = RealDrivingDetectionEngine(FakeActivitySource(false), FakeLocationSource(), this)
        engine.startMonitoring()
        assertEquals(MonitoringState.ACTIVITY_PERMISSION_REQUIRED, engine.monitoringState.value)
    }

    @Test fun `vehicle entry requests precise location permission when missing`() = runTest {
        val activity = FakeActivitySource()
        val engine = RealDrivingDetectionEngine(activity, FakeLocationSource(false), this)
        engine.startMonitoring(); runCurrent()
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER)); runCurrent()
        assertEquals(MonitoringState.LOCATION_PERMISSION_REQUIRED, engine.monitoringState.value)
        engine.stopMonitoring()
    }

    @Test fun `passenger decision is kept until vehicle exit`() = runTest {
        val activity = FakeActivitySource()
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(activity, location, this, config)
        engine.startMonitoring(); runCurrent()
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER)); runCurrent()
        location.events.emit(sample(6f, 1_000)); location.events.emit(sample(7f, 1_500)); location.events.emit(sample(8f, 2_000)); runCurrent()

        engine.markPassenger()
        assertEquals(DriverDecision.PASSENGER, engine.driverDecision.value)
        assertEquals(DriveState.IDLE, engine.driveState.value)
        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.ENTER)); runCurrent()
        assertEquals(DriveState.IDLE, engine.driveState.value)

        activity.events.emit(ActivityTransitionSignal(RecognizedActivity.IN_VEHICLE, TransitionType.EXIT)); runCurrent()
        assertEquals(DriverDecision.UNKNOWN, engine.driverDecision.value)
        engine.stopMonitoring()
    }

    @Test fun `driver decision is ignored outside confirmation state`() = runTest {
        val engine = RealDrivingDetectionEngine(FakeActivitySource(), FakeLocationSource(), this)
        engine.confirmDriver()
        engine.markPassenger()
        assertEquals(DriverDecision.UNKNOWN, engine.driverDecision.value)
        assertEquals(DriveState.IDLE, engine.driveState.value)
    }
}

private fun sample(speed: Float, time: Long, accuracy: Float = 10f) = LocationSample(0.0, 0.0, speed, accuracy, time)

private class FakeActivitySource(private val permission: Boolean = true) : ActivityRecognitionDataSource {
    val events = MutableSharedFlow<ActivityTransitionSignal>(extraBufferCapacity = 8)
    override val signals = events
    override fun hasPermission() = permission
    override fun start(onResult: (Result<Unit>) -> Unit) = onResult(Result.success(Unit))
    override fun stop() = Unit
}

private class FakeLocationSource(private val permission: Boolean = true) : LocationDataSource {
    val events = MutableSharedFlow<LocationSample>(extraBufferCapacity = 8)
    override val samples = events
    var stopCount = 0
    override fun hasPreciseLocationPermission() = permission
    override fun start(onResult: (Result<Unit>) -> Unit) = onResult(Result.success(Unit))
    override fun stop() { stopCount += 1 }
}
