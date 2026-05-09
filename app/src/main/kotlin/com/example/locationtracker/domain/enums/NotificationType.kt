package com.example.locationtracker.domain.enums

/**
 * Defines the different types of push notifications.
 * Each type can have specific handling logic and UI representation.
 */
enum class NotificationType(val channelId: String, val priority: Int) {
    /**
     * Friend request notifications (High priority)
     * e.g., "John Doe sent you a friend request"
     */
    FRIEND_REQUEST("friend_requests", 5),

    /**
     * Friend request accepted notifications (Normal priority)
     * e.g., "Jane accepted your friend request"
     */
    FRIEND_REQUEST_ACCEPTED("friend_updates", 3),

    /**
     * Location update notifications (Normal priority)
     * e.g., "Sarah arrived at Work"
     */
    LOCATION_UPDATE("location_updates", 3),

    /**
     * Family circle notifications (Normal priority)
     * e.g., "You were added to Smith Family circle"
     */
    FAMILY_CIRCLE("family_updates", 3),

    /**
     * Low battery alerts (High priority)
     * e.g., "John's phone battery is low (5%)"
     */
    LOW_BATTERY("battery_alerts", 5),

    /**
     * Geofencing notifications (High priority)
     * e.g., "Friend is nearby"
     */
    GEOFENCE("geofence_alerts", 5),

    /**
     * General app updates (Low priority)
     * e.g., "New features available"
     */
    GENERAL("general_updates", 2);

    companion object {
        /**
         * Parse notification type from string value
         */
        fun fromString(value: String): NotificationType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GENERAL
        }
    }
}