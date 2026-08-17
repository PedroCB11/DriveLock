package com.drivelock.app.domain.model

enum class DriveState {
    IDLE,
    MOVEMENT_DETECTED,
    POSSIBLE_VEHICLE,
    CONFIRMING_DRIVER,
    DRIVING,
    POSSIBLE_TRIP_END,
}

