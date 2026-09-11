package com.drivelock.app.ui.history

import com.drivelock.app.domain.model.Trip

data class HistoryStats(
    val tripCount: Int,
    val totalDurationMinutes: Long,
    val totalDistanceKm: Double,
    val avoidedNotifications: Int,
    val notificationsByPackage: Map<String, Int>,
)

fun List<Trip>.toHistoryStats(): HistoryStats = HistoryStats(
    tripCount = size,
    totalDurationMinutes = sumOf { it.durationMillis ?: 0L } / 60_000L,
    totalDistanceKm = sumOf { it.distanceMeters } / 1_000.0,
    avoidedNotifications = sumOf { it.blockedNotifications.values.sum() },
    notificationsByPackage = flatMap { it.blockedNotifications.entries }
        .groupingBy { it.key }
        .fold(0) { total, entry -> total + entry.value },
)
