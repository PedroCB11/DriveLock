package com.drivelock.app.ui.driving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.detection.DrivingDetectionEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DrivingViewModel(private val engine: DrivingDetectionEngine) : ViewModel() {
    val uiState = engine.driveState.map { driveState -> DrivingUiState(driveState = driveState) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DrivingUiState())

    fun confirmDriver() = engine.confirmDriver()
    fun markPassenger() = engine.markPassenger()
    fun endTrip() = engine.endTrip()
    fun reset() = engine.reset()

    class Factory(private val engine: DrivingDetectionEngine) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DrivingViewModel(engine) as T
    }
}
