package com.drivelock.app.detection.location

import com.drivelock.app.detection.DetectionConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleSpeedVerifierTest {
    private val verifier = VehicleSpeedVerifier(DetectionConfig(5f, 1_000, 3, 30f))

    @Test fun `accepts sustained ordered accurate vehicle speed`() {
        assertFalse(verifier.add(sample(6f, 0)))
        assertFalse(verifier.add(sample(6f, 500)))
        assertTrue(verifier.add(sample(6f, 1_000)))
    }

    @Test fun `rejects missing speed poor accuracy and out of order samples`() {
        assertFalse(verifier.add(sample(null, 0)))
        assertFalse(verifier.add(sample(8f, 1_000, 50f)))
        assertFalse(verifier.add(sample(8f, 900)))
    }
}

private fun sample(speed: Float?, time: Long, accuracy: Float = 10f) = LocationSample(0.0, 0.0, speed, accuracy, time)
