package com.example.locationtracker.domain.repository

import com.example.locationtracker.domain.model.AppSettings
import com.example.locationtracker.domain.model.AppTheme
import com.example.locationtracker.domain.model.HistoryRetention
import com.example.locationtracker.domain.model.MapStyle
import com.example.locationtracker.domain.model.MeasurementUnits
import com.example.locationtracker.domain.model.UpdateFrequency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    
    suspend fun updateLocationUpdateFrequency(frequency: UpdateFrequency)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateFriendRequestNotifications(enabled: Boolean)
    suspend fun updateLocationAlertNotifications(enabled: Boolean)
    suspend fun updateActivityUpdateNotifications(enabled: Boolean)
    suspend fun updateShareLocationEnabled(enabled: Boolean)
    suspend fun updateLocationHistoryRetention(retention: HistoryRetention)
    suspend fun updateShareBatteryLevel(enabled: Boolean)
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateMapStyle(mapStyle: MapStyle)
    suspend fun updateUnits(units: MeasurementUnits)
    
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    
    suspend fun clearAllSettings()
}
