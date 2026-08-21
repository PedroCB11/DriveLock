package com.drivelock.app.ui.driving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.detection.DrivingDetectionEngine
import com.drivelock.app.tracking.TripSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DrivingViewModel(
    private val engine: DrivingDetectionEngine,
    sessionManager: TripSessionManager,
) : ViewModel() {
    val uiState = combine(engine.driveState, engine.driverDecision, sessionManager.state) { driveState, decision, session ->
        DrivingUiState(
            driveState = driveState,
            driverDecision = decision,
            elapsedMinutes = (session.elapsedMillis / 60_000).toInt(),
            distanceKm = session.distanceMeters / 1_000,
            speedKph = session.currentSpeedMetersPerSecond * 3.6,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DrivingUiState())

    fun confirmDriver() = engine.confirmDriver()
    fun markPassenger() = engine.markPassenger()
    fun endTrip() = engine.endTrip()
    fun reset() = engine.reset()

    class Factory(
        private val engine: DrivingDetectionEngine,
        private val sessionManager: TripSessionManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DrivingViewModel(engine, sessionManager) as T
    }
}
