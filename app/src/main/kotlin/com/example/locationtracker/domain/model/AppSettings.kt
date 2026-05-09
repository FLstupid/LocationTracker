package com.example.locationtracker.domain.model

data class AppSettings(
    val locationUpdateFrequency: UpdateFrequency = UpdateFrequency.MEDIUM,
    val notificationsEnabled: Boolean = true,
    val friendRequestNotifications: Boolean = true,
    val locationAlertNotifications: Boolean = true,
    val activityUpdateNotifications: Boolean = true,
    val shareLocationEnabled: Boolean = true,
    val locationHistoryRetention: HistoryRetention = HistoryRetention.DAYS_30,
    val shareBatteryLevel: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val mapStyle: MapStyle = MapStyle.NORMAL,
    val units: MeasurementUnits = MeasurementUnits.METRIC
)

enum class UpdateFrequency(val intervalMinutes: Long) {
    HIGH(1),
    MEDIUM(5),
    LOW(15),
    BATTERY_SAVER(30)
}

enum class HistoryRetention(val days: Int) {
    DAYS_7(7),
    DAYS_30(30),
    DAYS_90(90),
    FOREVER(-1)
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

enum class MapStyle {
    NORMAL,
    SATELLITE,
    TERRAIN,
    HYBRID
}

enum class MeasurementUnits {
    METRIC,
    IMPERIAL
}
