package com.example.locationtracker.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentPage: Int = 0,
    val displayName: String = "",
    val locationPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val isCompleting: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun nextPage() {
        _state.update { it.copy(currentPage = it.currentPage + 1) }
    }

    fun previousPage() {
        _state.update { it.copy(currentPage = maxOf(0, it.currentPage - 1)) }
    }

    fun goToPage(page: Int) {
        _state.update { it.copy(currentPage = page) }
    }

    fun updateDisplayName(name: String) {
        _state.update { it.copy(displayName = name) }
    }

    fun onLocationPermissionGranted() {
        _state.update { it.copy(locationPermissionGranted = true) }
        nextPage()
    }

    fun onNotificationPermissionGranted() {
        _state.update { it.copy(notificationPermissionGranted = true) }
        nextPage()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            
            // Mark onboarding as completed
            settingsRepository.setOnboardingCompleted(true)
            
            // Save display name if provided (optional feature)
            // User can update profile later in Settings
            if (_state.value.displayName.isNotBlank()) {
                // Display name is available in state, can be used by ProfileScreen later
                android.util.Log.d("OnboardingViewModel", "Display name: ${_state.value.displayName}")
            }
            
            _state.update { it.copy(isCompleting = false) }
        }
    }
}
