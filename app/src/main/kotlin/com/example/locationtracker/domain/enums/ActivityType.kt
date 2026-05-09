package com.example.locationtracker.domain.enums

/**
 * Types of activities that can appear in the activity feed.
 * Each type represents a different kind of user action or event.
 */
enum class ActivityType(val displayName: String, val icon: String) {
    /**
     * Friend request was accepted
     */
    FRIEND_REQUEST_ACCEPTED("Friend request accepted", "✓"),

    /**
     * User started sharing their location
     */
    LOCATION_SHARED("Started sharing location", "📍"),

    /**
     * User arrived at a location
     */
    ARRIVED_AT_LOCATION("Arrived at location", "🏠"),

    /**
     * User left a location
     */
    LEFT_LOCATION("Left location", "🚗"),

    /**
     * User joined a family circle
     */
    JOINED_FAMILY_CIRCLE("Joined family circle", "👨‍👩‍👧"),

    /**
     * User's battery is low
     */
    LOW_BATTERY_WARNING("Battery is low", "🔋"),

    /**
     * User came online
     */
    USER_ONLINE("Came online", "🟢"),

    /**
     * User went offline
     */
    USER_OFFLINE("Went offline", "⚫");

    companion object {
        fun fromString(value: String): ActivityType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: LOCATION_SHARED
        }
    }
}