package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.flow.StateFlow

interface DrivingDetectionEngine {
    val driveState: StateFlow<DriveState>
    fun startMonitoring()
    fun stopMonitoring()
}

