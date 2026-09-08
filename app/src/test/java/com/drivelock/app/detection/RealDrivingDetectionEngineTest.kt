package com.drivelock.app.detection

import com.drivelock.app.detection.location.LocationDataSource
import com.drivelock.app.detection.location.LocationSample
import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.tracking.TripTrackingController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RealDrivingDetectionEngineTest {
    private val config = DetectionConfig(
        minimumVehicleSpeedMetersPerSecond = 5.5556f,
        maximumLocationAccuracyMeters = 30f,
        tripEndStationaryDurationMillis = 180_000,
    )

    @Test fun `speed above twenty kilometers per hour requests driver confirmation`() = runTest {
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(location, this, config)
        engine.startMonitoring(); runCurrent()

        location.events.emit(sample(5.6f)); runCurrent()

        assertEquals(DriveState.CONFIRMING_DRIVER, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test fun `speed below threshold and inaccurate samples do not start trip`() = runTest {
        val location = FakeLocationSource()
        val engine = RealDrivingDetectionEngine(location, this, config)
        engine.startMonitoring(); runCurrent()
        location.events.emit(sample(5.5f));
        location.events.emit(sample(20f, accuracy = 100f)); runCurrent()
        assertEquals(DriveState.IDLE, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test fun `missing precise location requests foreground permission`() = runTest {
        val engine = RealDrivingDetectionEngine(FakeLocationSource(precise = false), this)
        engine.startMonitoring()
        assertEquals(MonitoringState.LOCATION_PERMISSION_REQUIRED, engine.monitoringState.value)
    }

    @Test fun `missing background location requests background permission`() = runTest {
        val engine = RealDrivingDetectionEngine(FakeLocationSource(background = false), this)
        engine.startMonitoring()
        assertEquals(MonitoringState.BACKGROUND_LOCATION_PERMISSION_REQUIRED, engine.monitoringState.value)
    }

    @Test fun `three continuous low speed minutes end active trip`() = runTest {
        val location = FakeLocationSource()
        val tracking = FakeTrackingController()
        val engine = RealDrivingDetectionEngine(location, this, config, tripTrackingController = tracking)
        engine.startMonitoring(); runCurrent(); location.events.emit(sample(6f)); runCurrent(); engine.confirmDriver()

        engine.onLocationSample(sample(0f)); advanceTimeBy(180_001); runCurrent()

        assertEquals(1, tracking.stopCount)
        assertEquals(DriveState.POSSIBLE_TRIP_END, engine.driveState.value)
    }

    @Test fun `speed recovery cancels trip end countdown`() = runTest {
        val location = FakeLocationSource()
        val tracking = FakeTrackingController()
        val engine = RealDrivingDetectionEngine(location, this, config, tripTrackingController = tracking)
        engine.startMonitoring(); runCurrent(); location.events.emit(sample(6f)); runCurrent(); engine.confirmDriver()

        engine.onLocationSample(sample(0f)); advanceTimeBy(120_000)
        engine.onLocationSample(sample(8f)); advanceTimeBy(180_001); runCurrent()

        assertEquals(0, tracking.stopCount)
        assertEquals(DriveState.DRIVING, engine.driveState.value)
        engine.stopMonitoring()
    }

    @Test fun `confirming driver transfers monitoring to trip service`() = runTest {
        val location = FakeLocationSource()
        val monitor = FakeMonitorController()
        val tracking = FakeTrackingController()
        val engine = RealDrivingDetectionEngine(location, this, config, monitor, tracking)
        engine.startMonitoring(); runCurrent(); location.events.emit(sample(6f)); runCurrent()

        engine.confirmDriver()

        assertEquals(1, monitor.stopCount)
        assertEquals(1, tracking.startCount)
        assertEquals(DriveState.DRIVING, engine.driveState.value)
        engine.stopMonitoring()
    }
}

private fun sample(speed: Float, accuracy: Float = 10f) = LocationSample(0.0, 0.0, speed, accuracy, 1_000)

private class FakeLocationSource(
    private val precise: Boolean = true,
    private val background: Boolean = true,
) : LocationDataSource {
    val events = MutableSharedFlow<LocationSample>(extraBufferCapacity = 8)
    override val samples = events
    override fun hasPreciseLocationPermission() = precise
    override fun hasBackgroundLocationPermission() = background
    override fun start(onResult: (Result<Unit>) -> Unit) = onResult(Result.success(Unit))
    override fun stop() = Unit
}

private class FakeMonitorController : DrivingMonitorController {
    var stopCount = 0
    override fun start() = Result.success(Unit)
    override fun stop() { stopCount += 1 }
}

private class FakeTrackingController : TripTrackingController {
    var startCount = 0
    var stopCount = 0
    override fun start(): Result<Unit> { startCount += 1; return Result.success(Unit) }
    override fun stop() { stopCount += 1 }
}
