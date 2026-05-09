package com.example.locationtracker.domain.model

import com.example.locationtracker.domain.enums.PresenceStatus

/**
 * Represents detailed presence information for a user
 */
data class UserPresence(
    val userId: String = "",
    val status: PresenceStatus = PresenceStatus.OFFLINE,
    val lastSeen: Long = 0L, // Timestamp in milliseconds
    val batteryLevel: Int? = null, // 0-100, null if unknown
    val isSharing: Boolean = false, // Is currently sharing location
    val currentPlace: String? = null // Name of current place if available
) {
    /**
     * Get human-readable last seen text
     */
    fun getLastSeenText(): String {
        if (status == PresenceStatus.ONLINE) {
            return "Active now"
        }

        val now = System.currentTimeMillis()
        val minutesElapsed = (now - lastSeen) / (1000 * 60)

        return when {
            minutesElapsed < 1 -> "Active now"
            minutesElapsed < 60 -> "Last seen ${minutesElapsed}m ago"
            minutesElapsed < 1440 -> { // Less than 24 hours
                val hours = minutesElapsed / 60
                "Last seen ${hours}h ago"
            }
            else -> {
                val days = minutesElapsed / 1440
                "Last seen ${days}d ago"
            }
        }
    }

    /**
     * Check if battery is low (< 20%)
     */
    fun isBatteryLow(): Boolean = batteryLevel != null && batteryLevel < 20
}
