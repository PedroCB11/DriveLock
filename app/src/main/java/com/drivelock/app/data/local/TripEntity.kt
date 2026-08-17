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
)

fun TripEntity.toDomain() = Trip(
    id, startTime, endTime, durationMillis, distanceMeters, averageSpeedKph, maxSpeedKph,
    startLatitude, startLongitude, endLatitude, endLongitude,
)

fun Trip.toEntity() = TripEntity(
    id, startTime, endTime, durationMillis, distanceMeters, averageSpeedKph, maxSpeedKph,
    startLatitude, startLongitude, endLatitude, endLongitude,
)

