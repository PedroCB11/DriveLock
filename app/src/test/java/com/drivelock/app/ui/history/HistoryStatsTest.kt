package com.drivelock.app.ui.history

import com.drivelock.app.domain.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryStatsTest {
    @Test fun `aggregates trip impact and notification counts`() {
        val trips = listOf(
            Trip(startTime = 1, durationMillis = 120_000, distanceMeters = 2_500.0, blockedNotifications = mapOf("chat" to 3)),
            Trip(startTime = 2, durationMillis = 60_000, distanceMeters = 1_000.0, blockedNotifications = mapOf("chat" to 2, "social" to 4)),
        )

        val stats = trips.toHistoryStats()

        assertEquals(2, stats.tripCount)
        assertEquals(3, stats.totalDurationMinutes)
        assertEquals(3.5, stats.totalDistanceKm, 0.0)
        assertEquals(9, stats.avoidedNotifications)
        assertEquals(mapOf("chat" to 5, "social" to 4), stats.notificationsByPackage)
    }

    @Test fun `empty history produces zero impact`() {
        val stats = emptyList<Trip>().toHistoryStats()
        assertEquals(0, stats.tripCount)
        assertEquals(0, stats.avoidedNotifications)
        assertEquals(emptyMap<String, Int>(), stats.notificationsByPackage)
    }
}
