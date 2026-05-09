package com.example.locationtracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.domain.model.AppSettings
import com.example.locationtracker.domain.model.AppTheme
import com.example.locationtracker.domain.model.HistoryRetention
import com.example.locationtracker.domain.model.MapStyle
import com.example.locationtracker.domain.model.MeasurementUnits
import com.example.locationtracker.domain.model.UpdateFrequency
import com.example.locationtracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateLocationUpdateFrequency(frequency: UpdateFrequency) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLocationUpdateFrequency(frequency)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateFriendRequestNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateFriendRequestNotifications(enabled)
        }
    }

    fun updateLocationAlertNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLocationAlertNotifications(enabled)
        }
    }

    fun updateActivityUpdateNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateActivityUpdateNotifications(enabled)
        }
    }

    fun updateShareLocationEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateShareLocationEnabled(enabled)
        }
    }

    fun updateLocationHistoryRetention(retention: HistoryRetention) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLocationHistoryRetention(retention)
        }
    }

    fun updateShareBatteryLevel(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateShareBatteryLevel(enabled)
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateTheme(theme)
        }
    }

    fun updateMapStyle(mapStyle: MapStyle) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateMapStyle(mapStyle)
        }
    }

    fun updateUnits(units: MeasurementUnits) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateUnits(units)
        }
    }

    fun clearAllSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.clearAllSettings()
        }
    }
}
