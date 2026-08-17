package com.drivelock.app.data.repository

import com.drivelock.app.data.local.TripDao
import com.drivelock.app.data.local.toDomain
import com.drivelock.app.data.local.toEntity
import com.drivelock.app.domain.model.Trip
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(private val dao: TripDao) : TripRepository {
    override fun getAllTrips(): Flow<List<Trip>> = dao.getAllTrips().map { list -> list.map { it.toDomain() } }
    override fun getTripById(id: Long): Flow<Trip?> = dao.getTripById(id).map { it?.toDomain() }
    override suspend fun insertTrip(trip: Trip): Long = dao.insertTrip(trip.toEntity())
    override suspend fun deleteTrip(trip: Trip) = dao.deleteTrip(trip.toEntity())
    override suspend fun deleteAllTrips() = dao.deleteAllTrips()
}

