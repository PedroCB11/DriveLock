package com.drivelock.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val tripRepository: TripRepository) : ViewModel() {
    private val mutableHistoryCleared = MutableStateFlow(false)
    val historyCleared: StateFlow<Boolean> = mutableHistoryCleared.asStateFlow()

    fun clearHistory() {
        viewModelScope.launch {
            tripRepository.deleteAllTrips()
            mutableHistoryCleared.value = true
        }
    }

    fun consumeHistoryCleared() { mutableHistoryCleared.value = false }

    class Factory(private val tripRepository: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(tripRepository) as T
    }
}
