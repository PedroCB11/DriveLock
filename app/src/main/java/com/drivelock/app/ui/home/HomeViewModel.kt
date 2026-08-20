package com.drivelock.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.detection.DrivingDetectionEngine
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: TripRepository,
    private val detectionEngine: DrivingDetectionEngine,
) : ViewModel() {
    val uiState = combine(detectionEngine.driveState, detectionEngine.monitoringState, repository.getAllTrips()) { state, monitoring, trips ->
        HomeUiState(state, trips.firstOrNull(), false, monitoring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun startMonitoring() = detectionEngine.startMonitoring()
    fun reset() = detectionEngine.reset()

    class Factory(
        private val repository: TripRepository,
        private val engine: DrivingDetectionEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository, engine) as T
    }
}
