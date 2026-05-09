package com.example.locationtracker.data.repository

import com.example.locationtracker.data.local.preferences.PreferencesDataSource
import com.example.locationtracker.domain.model.AppSettings
import com.example.locationtracker.domain.model.AppTheme
import com.example.locationtracker.domain.model.HistoryRetention
import com.example.locationtracker.domain.model.MapStyle
import com.example.locationtracker.domain.model.MeasurementUnits
import com.example.locationtracker.domain.model.UpdateFrequency
import com.example.locationtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return preferencesDataSource.settingsFlow
    }

    override suspend fun updateLocationUpdateFrequency(frequency: UpdateFrequency) {
        preferencesDataSource.updateLocationUpdateFrequency(frequency)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        preferencesDataSource.updateNotificationsEnabled(enabled)
    }

    override suspend fun updateFriendRequestNotifications(enabled: Boolean) {
        preferencesDataSource.updateFriendRequestNotifications(enabled)
    }

    override suspend fun updateLocationAlertNotifications(enabled: Boolean) {
        preferencesDataSource.updateLocationAlertNotifications(enabled)
    }

    override suspend fun updateActivityUpdateNotifications(enabled: Boolean) {
        preferencesDataSource.updateActivityUpdateNotifications(enabled)
    }

    override suspend fun updateShareLocationEnabled(enabled: Boolean) {
        preferencesDataSource.updateShareLocationEnabled(enabled)
    }

    override suspend fun updateLocationHistoryRetention(retention: HistoryRetention) {
        preferencesDataSource.updateLocationHistoryRetention(retention)
    }

    override suspend fun updateShareBatteryLevel(enabled: Boolean) {
        preferencesDataSource.updateShareBatteryLevel(enabled)
    }

    override suspend fun updateTheme(theme: AppTheme) {
        preferencesDataSource.updateTheme(theme)
    }

    override suspend fun updateMapStyle(mapStyle: MapStyle) {
        preferencesDataSource.updateMapStyle(mapStyle)
    }

    override suspend fun updateUnits(units: MeasurementUnits) {
        preferencesDataSource.updateUnits(units)
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return preferencesDataSource.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataSource.setOnboardingCompleted(completed)
    }

    override suspend fun clearAllSettings() {
        preferencesDataSource.clearAllSettings()
    }
}
