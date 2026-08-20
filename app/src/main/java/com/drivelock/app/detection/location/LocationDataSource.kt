package com.drivelock.app.detection.location

import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    val samples: Flow<LocationSample>
    fun hasPreciseLocationPermission(): Boolean
    fun start(onResult: (Result<Unit>) -> Unit)
    fun stop()
}
