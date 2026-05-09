package com.example.locationtracker.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.locationtracker.domain.model.AppSettings
import com.example.locationtracker.domain.model.AppTheme
import com.example.locationtracker.domain.model.HistoryRetention
import com.example.locationtracker.domain.model.MapStyle
import com.example.locationtracker.domain.model.MeasurementUnits
import com.example.locationtracker.domain.model.UpdateFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val LOCATION_UPDATE_FREQUENCY = stringPreferencesKey("location_update_frequency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FRIEND_REQUEST_NOTIFICATIONS = booleanPreferencesKey("friend_request_notifications")
        val LOCATION_ALERT_NOTIFICATIONS = booleanPreferencesKey("location_alert_notifications")
        val ACTIVITY_UPDATE_NOTIFICATIONS = booleanPreferencesKey("activity_update_notifications")
        val SHARE_LOCATION_ENABLED = booleanPreferencesKey("share_location_enabled")
        val LOCATION_HISTORY_RETENTION = stringPreferencesKey("location_history_retention")
        val SHARE_BATTERY_LEVEL = booleanPreferencesKey("share_battery_level")
        val THEME = stringPreferencesKey("theme")
        val MAP_STYLE = stringPreferencesKey("map_style")
        val UNITS = stringPreferencesKey("units")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            locationUpdateFrequency = preferences[PreferencesKeys.LOCATION_UPDATE_FREQUENCY]
                ?.let { UpdateFrequency.valueOf(it) } ?: UpdateFrequency.MEDIUM,
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            friendRequestNotifications = preferences[PreferencesKeys.FRIEND_REQUEST_NOTIFICATIONS] ?: true,
            locationAlertNotifications = preferences[PreferencesKeys.LOCATION_ALERT_NOTIFICATIONS] ?: true,
            activityUpdateNotifications = preferences[PreferencesKeys.ACTIVITY_UPDATE_NOTIFICATIONS] ?: true,
            shareLocationEnabled = preferences[PreferencesKeys.SHARE_LOCATION_ENABLED] ?: true,
            locationHistoryRetention = preferences[PreferencesKeys.LOCATION_HISTORY_RETENTION]
                ?.let { HistoryRetention.valueOf(it) } ?: HistoryRetention.DAYS_30,
            shareBatteryLevel = preferences[PreferencesKeys.SHARE_BATTERY_LEVEL] ?: true,
            theme = preferences[PreferencesKeys.THEME]
                ?.let { AppTheme.valueOf(it) } ?: AppTheme.SYSTEM,
            mapStyle = preferences[PreferencesKeys.MAP_STYLE]
                ?.let { MapStyle.valueOf(it) } ?: MapStyle.NORMAL,
            units = preferences[PreferencesKeys.UNITS]
                ?.let { MeasurementUnits.valueOf(it) } ?: MeasurementUnits.METRIC
        )
    }

    suspend fun updateLocationUpdateFrequency(frequency: UpdateFrequency) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_UPDATE_FREQUENCY] = frequency.name
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateFriendRequestNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FRIEND_REQUEST_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateLocationAlertNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_ALERT_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateActivityUpdateNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVITY_UPDATE_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateShareLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHARE_LOCATION_ENABLED] = enabled
        }
    }

    suspend fun updateLocationHistoryRetention(retention: HistoryRetention) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_HISTORY_RETENTION] = retention.name
        }
    }

    suspend fun updateShareBatteryLevel(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHARE_BATTERY_LEVEL] = enabled
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun updateMapStyle(mapStyle: MapStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAP_STYLE] = mapStyle.name
        }
    }

    suspend fun updateUnits(units: MeasurementUnits) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNITS] = units.name
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data
            .map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }
            .first()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
