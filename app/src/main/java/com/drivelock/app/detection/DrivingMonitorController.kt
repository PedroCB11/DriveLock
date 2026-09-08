package com.drivelock.app.detection

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

interface DrivingMonitorController {
    fun start(): Result<Unit>
    fun stop()
}

class AndroidDrivingMonitorController(private val context: Context) : DrivingMonitorController {
    override fun start(): Result<Unit> = runCatching {
        ContextCompat.startForegroundService(context, Intent(context, DrivingMonitorService::class.java))
    }.map { Unit }

    override fun stop() {
        context.stopService(Intent(context, DrivingMonitorService::class.java))
    }
}

object NoOpDrivingMonitorController : DrivingMonitorController {
    override fun start() = Result.success(Unit)
    override fun stop() = Unit
}
