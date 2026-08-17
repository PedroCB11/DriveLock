package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeDrivingDetectionEngineTest {
    @Test fun `development controls transition through driving flow`() {
        val engine = FakeDrivingDetectionEngine()
        engine.simulateMovement()
        assertEquals(DriveState.MOVEMENT_DETECTED, engine.driveState.value)
        engine.simulateVehicleDetection()
        assertEquals(DriveState.CONFIRMING_DRIVER, engine.driveState.value)
        engine.confirmDriver()
        assertEquals(DriveState.DRIVING, engine.driveState.value)
        engine.simulateTripEnd()
        assertEquals(DriveState.POSSIBLE_TRIP_END, engine.driveState.value)
        engine.reset()
        assertEquals(DriveState.IDLE, engine.driveState.value)
    }
}
