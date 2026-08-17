package com.drivelock.app.domain.repository

import com.drivelock.app.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getAllTrips(): Flow<List<Trip>>
    fun getTripById(id: Long): Flow<Trip?>
    suspend fun insertTrip(trip: Trip): Long
    suspend fun deleteTrip(trip: Trip)
    suspend fun deleteAllTrips()
}

