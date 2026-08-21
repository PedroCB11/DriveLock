package com.drivelock.app.tracking

interface TripTrackingController {
    fun start(): Result<Unit>
    fun stop()
}

object NoOpTripTrackingController : TripTrackingController {
    override fun start() = Result.success(Unit)
    override fun stop() = Unit
}
