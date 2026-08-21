package com.drivelock.app.tracking

import com.drivelock.app.detection.location.LocationSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripSessionManagerTest {
    @Test fun `tracks elapsed distance current average and maximum speed`() {
        val manager = TripSessionManager()
        manager.start(startTimeMillis = 10_000, elapsedRealtimeMillis = 1_000)
        manager.addLocation(sample(latitude = 0.0, time = 1_000, speed = 10f))
        manager.addLocation(sample(latitude = 0.001, time = 11_000, speed = 15f))

        val state = manager.state.value
        assertTrue(state.isActive)
        assertEquals(10_000, state.elapsedMillis)
        assertEquals(111.2, state.distanceMeters, 1.0)
        assertEquals(15f, state.currentSpeedMetersPerSecond)
        assertEquals(15f, state.maximumSpeedMetersPerSecond)
        assertEquals(state.distanceMeters / 10.0, state.averageSpeedMetersPerSecond, 0.01)
    }

    @Test fun `rejects inaccurate out of order and impossible jumps`() {
        val manager = TripSessionManager()
        manager.start(10_000, 1_000)
        manager.addLocation(sample(0.0, 1_000, 5f))
        manager.addLocation(sample(0.001, 900, 5f))
        manager.addLocation(sample(1.0, 2_000, 5f))
        manager.addLocation(sample(0.001, 3_000, 5f, accuracy = 100f))

        assertEquals(0.0, manager.state.value.distanceMeters, 0.0)
    }

    @Test fun `end freezes final metrics and marks session inactive`() {
        val manager = TripSessionManager()
        manager.start(10_000, 1_000)
        manager.updateElapsed(61_000)
        val finalState = manager.end()

        assertFalse(finalState.isActive)
        assertEquals(60_000, finalState.elapsedMillis)
        assertEquals(0f, finalState.currentSpeedMetersPerSecond)
    }
}

private fun sample(latitude: Double, time: Long, speed: Float, accuracy: Float = 10f) = LocationSample(
    latitude = latitude,
    longitude = 0.0,
    speedMetersPerSecond = speed,
    accuracyMeters = accuracy,
    elapsedRealtimeMillis = time,
)
