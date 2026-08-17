package com.drivelock.app.data.local

import com.drivelock.app.domain.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Test

class TripMappingTest {
    @Test fun `entity mapping round trip preserves every field`() {
        val trip = Trip(7, 100, 200, 100, 1250.0, 45.0, 70.0, -23.5, -46.6, -23.6, -46.7)
        assertEquals(trip, trip.toEntity().toDomain())
    }
}

