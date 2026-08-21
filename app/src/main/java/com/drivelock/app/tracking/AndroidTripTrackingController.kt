package com.drivelock.app.tracking

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AndroidTripTrackingController(private val context: Context) : TripTrackingController {
    override fun start(): Result<Unit> = runCatching {
        ContextCompat.startForegroundService(
            context,
            Intent(context, TripTrackingService::class.java).setAction(TripTrackingService.ACTION_START),
        )
    }.map { Unit }

    override fun stop() {
        context.stopService(Intent(context, TripTrackingService::class.java))
    }
}
