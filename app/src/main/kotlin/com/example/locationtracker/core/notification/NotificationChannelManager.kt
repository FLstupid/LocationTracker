package com.example.locationtracker.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.locationtracker.domain.enums.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notification channels for Android 8.0 (API 26) and above.
 * Creates and maintains notification channels with appropriate importance levels.
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    companion object {
        // Channel IDs
        const val CHANNEL_FRIEND_REQUESTS = "friend_requests"
        const val CHANNEL_FRIEND_UPDATES = "friend_updates"
        const val CHANNEL_LOCATION_UPDATES = "location_updates"
        const val CHANNEL_FAMILY_UPDATES = "family_updates"
        const val CHANNEL_BATTERY_ALERTS = "battery_alerts"
        const val CHANNEL_GEOFENCE_ALERTS = "geofence_alerts"
        const val CHANNEL_GENERAL = "general_updates"
    }
    
    /**
     * Create all notification channels.
     * Should be called when the app starts.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // High Priority Channels
            val friendRequestsChannel = NotificationChannel(
                CHANNEL_FRIEND_REQUESTS,
                "Friend Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new friend requests"
                enableLights(true)
                enableVibration(true)
            }
            
            val batteryAlertsChannel = NotificationChannel(
                CHANNEL_BATTERY_ALERTS,
                "Battery Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when friends' batteries are low"
                enableLights(true)
                enableVibration(true)
            }
            
            val geofenceChannel = NotificationChannel(
                CHANNEL_GEOFENCE_ALERTS,
                "Location Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when friends are nearby or enter/leave locations"
                enableLights(true)
                enableVibration(true)
            }
            
            // Normal Priority Channels
            val friendUpdatesChannel = NotificationChannel(
                CHANNEL_FRIEND_UPDATES,
                "Friend Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates about friend requests and friend activities"
                enableLights(true)
            }
            
            val locationUpdatesChannel = NotificationChannel(
                CHANNEL_LOCATION_UPDATES,
                "Location Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when friends arrive or leave locations"
                enableLights(true)
            }
            
            val familyUpdatesChannel = NotificationChannel(
                CHANNEL_FAMILY_UPDATES,
                "Family Circle Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates about family circle activities"
                enableLights(true)
            }
            
            // Low Priority Channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app updates and announcements"
            }
            
            // Create all channels
            notificationManager.createNotificationChannels(
                listOf(
                    friendRequestsChannel,
                    batteryAlertsChannel,
                    geofenceChannel,
                    friendUpdatesChannel,
                    locationUpdatesChannel,
                    familyUpdatesChannel,
                    generalChannel
                )
            )
        }
    }
    
    /**
     * Get the channel ID for a notification type
     */
    fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.FRIEND_REQUEST -> CHANNEL_FRIEND_REQUESTS
            NotificationType.FRIEND_REQUEST_ACCEPTED -> CHANNEL_FRIEND_UPDATES
            NotificationType.LOCATION_UPDATE -> CHANNEL_LOCATION_UPDATES
            NotificationType.FAMILY_CIRCLE -> CHANNEL_FAMILY_UPDATES
            NotificationType.LOW_BATTERY -> CHANNEL_BATTERY_ALERTS
            NotificationType.GEOFENCE -> CHANNEL_GEOFENCE_ALERTS
            NotificationType.GENERAL -> CHANNEL_GENERAL
        }
    }
    
    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Check if a specific channel is enabled
     */
    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return areNotificationsEnabled()
    }
    
    /**
     * Delete all notification channels (useful for testing or reset)
     */
    fun deleteAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notificationChannels.forEach { channel ->
                notificationManager.deleteNotificationChannel(channel.id)
            }
        }
    }
}
