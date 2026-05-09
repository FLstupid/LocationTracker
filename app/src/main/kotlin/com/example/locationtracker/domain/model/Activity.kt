package com.example.locationtracker.domain.model

import com.example.locationtracker.domain.enums.ActivityType
import java.util.Date

/**
 * Represents an activity in the user's feed.
 * Activities are events that occur related to friends and family.
 * 
 * @property id Unique identifier for the activity
 * @property userId User who performed the activity
 * @property userName Display name of the user
 * @property userPhotoUrl Optional photo URL of the user
 * @property type Type of activity
 * @property timestamp When the activity occurred
 * @property data Additional data specific to the activity type
 * @property isRead Whether the current user has seen this activity
 */
data class Activity(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String? = null,
    val type: ActivityType,
    val timestamp: Date,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false
) {
    /**
     * Get location name from activity data
     */
    fun getLocationName(): String? {
        return data["locationName"]
    }
    
    /**
     * Get family circle name from activity data
     */
    fun getFamilyCircleName(): String? {
        return data["circleName"]
    }
    
    /**
     * Get battery level from activity data
     */
    fun getBatteryLevel(): Int? {
        return data["batteryLevel"]?.toIntOrNull()
    }
    
    /**
     * Get friend name from activity data (for friend request accepted)
     */
    fun getFriendName(): String? {
        return data["friendName"]
    }
    
    /**
     * Format the activity message based on type and data
     */
    fun getFormattedMessage(): String {
        return when (type) {
            ActivityType.FRIEND_REQUEST_ACCEPTED -> {
                val friendName = getFriendName()
                if (friendName != null) {
                    "$userName and $friendName are now friends"
                } else {
                    "$userName accepted a friend request"
                }
            }
            ActivityType.LOCATION_SHARED -> {
                "$userName started sharing location"
            }
            ActivityType.ARRIVED_AT_LOCATION -> {
                val locationName = getLocationName()
                if (locationName != null) {
                    "$userName arrived at $locationName"
                } else {
                    "$userName arrived at a location"
                }
            }
            ActivityType.LEFT_LOCATION -> {
                val locationName = getLocationName()
                if (locationName != null) {
                    "$userName left $locationName"
                } else {
                    "$userName left a location"
                }
            }
            ActivityType.JOINED_FAMILY_CIRCLE -> {
                val circleName = getFamilyCircleName()
                if (circleName != null) {
                    "$userName joined $circleName"
                } else {
                    "$userName joined a family circle"
                }
            }
            ActivityType.LOW_BATTERY_WARNING -> {
                val batteryLevel = getBatteryLevel()
                if (batteryLevel != null) {
                    "$userName's battery is low ($batteryLevel%)"
                } else {
                    "$userName's battery is low"
                }
            }
            ActivityType.USER_ONLINE -> {
                "$userName is online"
            }
            ActivityType.USER_OFFLINE -> {
                "$userName went offline"
            }
        }
    }
    
    /**
     * Get relative time string (e.g., "5 minutes ago")
     */
    fun getRelativeTimeText(): String {
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp.time
        val diffMinutes = (diffMillis / 1000 / 60).toInt()
        
        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes min ago"
            diffMinutes < 1440 -> "${diffMinutes / 60} hr ago"
            diffMinutes < 10080 -> "${diffMinutes / 1440} days ago"
            else -> "${diffMinutes / 10080} weeks ago"
        }
    }
}
