package com.example.locationtracker.domain.model

import com.example.locationtracker.domain.enums.NotificationType
import java.util.Date

/**
 * Represents a push notification that can be displayed to the user.
 * 
 * @property id Unique identifier for the notification
 * @property title Notification title (shown in bold)
 * @property message Notification body text
 * @property type Type of notification (determines channel, priority, and handling)
 * @property data Additional key-value data for deep linking and context
 * @property timestamp When the notification was created
 * @property imageUrl Optional image to show in the notification
 * @property isRead Whether the user has seen/interacted with this notification
 */
data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Date = Date(),
    val imageUrl: String? = null,
    val isRead: Boolean = false
) {
    /**
     * Get the deep link destination from notification data
     */
    fun getDeepLink(): String? {
        return data["deepLink"]
    }
    
    /**
     * Get user ID from notification data (for friend/user related notifications)
     */
    fun getUserId(): String? {
        return data["userId"]
    }
    
    /**
     * Get family circle ID from notification data
     */
    fun getCircleId(): String? {
        return data["circleId"]
    }
    
    /**
     * Get location place name from notification data
     */
    fun getPlaceName(): String? {
        return data["placeName"]
    }
    
    /**
     * Check if notification has action buttons
     */
    fun hasActions(): Boolean {
        return type == NotificationType.FRIEND_REQUEST
    }
    
    /**
     * Get notification action labels based on type
     */
    fun getActionLabels(): Pair<String, String>? {
        return when (type) {
            NotificationType.FRIEND_REQUEST -> "Accept" to "Decline"
            else -> null
        }
    }
}

/**
 * Builder for creating PushNotification instances
 */
class PushNotificationBuilder {
    private var id: String = ""
    private var title: String = ""
    private var message: String = ""
    private var type: NotificationType = NotificationType.GENERAL
    private var data: Map<String, String> = emptyMap()
    private var timestamp: Date = Date()
    private var imageUrl: String? = null
    private var isRead: Boolean = false
    
    fun id(id: String) = apply { this.id = id }
    fun title(title: String) = apply { this.title = title }
    fun message(message: String) = apply { this.message = message }
    fun type(type: NotificationType) = apply { this.type = type }
    fun data(data: Map<String, String>) = apply { this.data = data }
    fun timestamp(timestamp: Date) = apply { this.timestamp = timestamp }
    fun imageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }
    fun isRead(isRead: Boolean) = apply { this.isRead = isRead }
    
    fun build() = PushNotification(
        id = id,
        title = title,
        message = message,
        type = type,
        data = data,
        timestamp = timestamp,
        imageUrl = imageUrl,
        isRead = isRead
    )
}
