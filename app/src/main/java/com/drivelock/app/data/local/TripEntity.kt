package com.drivelock.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drivelock.app.domain.model.Trip

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val durationMillis: Long?,
    val distanceMeters: Double,
    val averageSpeedKph: Double?,
    val maxSpeedKph: Double?,
    val startLatitude: Double?,
    val startLongitude: Double?,
    val endLatitude: Double?,
    val endLongitude: Double?,
    val notificationSummary: String = "",
)

fun TripEntity.toDomain() = Trip(
    id, startTime, endTime, durationMillis, distanceMeters, averageSpeedKph, maxSpeedKph,
    startLatitude, startLongitude, endLatitude, endLongitude, decodeNotificationSummary(notificationSummary),
)

fun Trip.toEntity() = TripEntity(
    id, startTime, endTime, durationMillis, distanceMeters, averageSpeedKph, maxSpeedKph,
    startLatitude, startLongitude, endLatitude, endLongitude, encodeNotificationSummary(blockedNotifications),
)

private fun encodeNotificationSummary(counts: Map<String, Int>): String =
    counts.entries.joinToString(";") { "${it.key}:${it.value}" }

private fun decodeNotificationSummary(value: String): Map<String, Int> = value
    .split(';')
    .mapNotNull { entry ->
        val separator = entry.lastIndexOf(':')
        if (separator <= 0) null else entry.substring(0, separator) to (entry.substring(separator + 1).toIntOrNull() ?: return@mapNotNull null)
    }
    .toMap()
