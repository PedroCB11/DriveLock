package com.drivelock.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.detection.FakeDrivingDetectionEngine
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: TripRepository,
    private val detectionEngine: FakeDrivingDetectionEngine,
) : ViewModel() {
    val uiState = combine(detectionEngine.driveState, repository.getAllTrips()) { state, trips ->
        HomeUiState(state, trips.firstOrNull(), false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun simulateMovement() = detectionEngine.simulateMovement()
    fun simulateVehicleDetection() = detectionEngine.simulateVehicleDetection()
    fun reset() = detectionEngine.reset()

    class Factory(
        private val repository: TripRepository,
        private val engine: FakeDrivingDetectionEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository, engine) as T
    }
}

