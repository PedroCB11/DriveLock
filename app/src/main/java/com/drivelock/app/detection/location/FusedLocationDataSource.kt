package com.drivelock.app.detection.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FusedLocationDataSource(private val context: Context) : LocationDataSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val mutableSamples = MutableSharedFlow<LocationSample>(extraBufferCapacity = 16)
    override val samples = mutableSamples.asSharedFlow()
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { location ->
                mutableSamples.tryEmit(
                    LocationSample(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speedMetersPerSecond = location.speed.takeIf { location.hasSpeed() },
                        accuracyMeters = location.accuracy,
                        elapsedRealtimeMillis = location.elapsedRealtimeNanos / 1_000_000,
                    ),
                )
            }
        }
    }

    override fun hasPreciseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun start(onResult: (Result<Unit>) -> Unit) {
        if (!hasPreciseLocationPermission()) {
            onResult(Result.failure(SecurityException("Precise foreground location permission is required")))
            return
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000)
            .setMinUpdateIntervalMillis(1_000)
            .setMinUpdateDistanceMeters(2f)
            .build()
        client.requestLocationUpdates(request, ContextCompat.getMainExecutor(context), callback)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun stop() {
        client.removeLocationUpdates(callback)
    }
}
