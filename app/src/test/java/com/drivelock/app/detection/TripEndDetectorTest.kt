package com.drivelock.app.detection

import com.drivelock.app.detection.location.LocationSample
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripEndDetectorTest {
    private val detector = TripEndDetector(
        DetectionConfig(
            tripEndMaximumSpeedMetersPerSecond = 1.5f,
            tripEndStationaryDurationMillis = 1_000,
        ),
    )

    @Test fun `vehicle exit and sustained low speed detect probable trip end`() {
        detector.startSession()
        detector.onLocation(sample(speed = 1f, time = 1_000))
        detector.onVehiclePresenceChanged(inVehicle = false, elapsedRealtimeMillis = 1_500)
        detector.tick(1_999)
        assertFalse(detector.probableTripEnd.value)
        detector.tick(2_000)
        assertTrue(detector.probableTripEnd.value)
    }

    @Test fun `low speed without vehicle exit does not end trip at traffic light`() {
        detector.startSession()
        detector.onLocation(sample(speed = 0f, time = 1_000))
        detector.tick(10_000)
        assertFalse(detector.probableTripEnd.value)
    }

    @Test fun `speed recovery cancels stationary window`() {
        detector.startSession()
        detector.onVehiclePresenceChanged(inVehicle = false, elapsedRealtimeMillis = 1_000)
        detector.onLocation(sample(speed = 0f, time = 1_000))
        detector.onLocation(sample(speed = 8f, time = 1_500))
        detector.tick(3_000)
        assertFalse(detector.probableTripEnd.value)
    }

    @Test fun `vehicle reentry cancels probable trip end`() {
        detector.startSession()
        detector.onVehiclePresenceChanged(false, 1_000)
        detector.onLocation(sample(0f, 1_000))
        detector.tick(2_000)
        assertTrue(detector.probableTripEnd.value)
        detector.onVehiclePresenceChanged(true, 2_100)
        assertFalse(detector.probableTripEnd.value)
    }
}

private fun sample(speed: Float, time: Long) = LocationSample(0.0, 0.0, speed, 10f, time)
