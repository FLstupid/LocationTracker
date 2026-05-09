package com.example.locationtracker.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.locationtracker.MainActivity
import com.example.locationtracker.R
import com.example.locationtracker.domain.enums.NotificationType
import com.example.locationtracker.domain.model.PushNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and showing notifications.
 * Handles different notification types with appropriate styling and actions.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val channelManager: NotificationChannelManager
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val REQUEST_CODE_OPEN = 100
        private const val REQUEST_CODE_ACCEPT = 101
        private const val REQUEST_CODE_DECLINE = 102
        
        // Intent extras
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_CIRCLE_ID = "circle_id"
        const val EXTRA_DEEP_LINK = "deep_link"
    }
    
    /**
     * Show a notification for the given push notification
     */
    fun showNotification(pushNotification: PushNotification) {
        helperScope.launch {
            val notification = buildNotification(pushNotification)
            val notificationId = pushNotification.id.hashCode()
            notificationManager.notify(notificationId, notification)
        }
    }
    
    /**
     * Build a notification from PushNotification data
     */
    private suspend fun buildNotification(pushNotification: PushNotification): Notification {
        val channelId = channelManager.getChannelId(pushNotification.type)
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(pushNotification.title)
            .setContentText(pushNotification.message)
            .setSmallIcon(getNotificationIcon(pushNotification.type))
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(pushNotification.type))
            .setContentIntent(createContentIntent(pushNotification))
        
        // Set style based on message length
        if (pushNotification.message.length > 40) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(pushNotification.message)
            )
        }
        
        // Load and set image if available
        pushNotification.imageUrl?.let { imageUrl ->
            val bitmap = loadImageFromUrl(imageUrl)
            bitmap?.let {
                builder.setLargeIcon(it)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
                )
            }
        }
        
        // Add action buttons for friend requests
        if (pushNotification.type == NotificationType.FRIEND_REQUEST) {
            addFriendRequestActions(builder, pushNotification)
        }
        
        // Set color
        builder.color = context.getColor(R.color.purple_500)
        
        return builder.build()
    }
    
    /**
     * Create pending intent for notification tap
     */
    private fun createContentIntent(pushNotification: PushNotification): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, pushNotification.type.name)
            pushNotification.getUserId()?.let { putExtra(EXTRA_USER_ID, it) }
            pushNotification.getCircleId()?.let { putExtra(EXTRA_CIRCLE_ID, it) }
            pushNotification.getDeepLink()?.let { putExtra(EXTRA_DEEP_LINK, it) }
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN + pushNotification.id.hashCode(),
            intent,
            flags
        )
    }
    
    /**
     * Add action buttons for friend request notifications
     */
    private fun addFriendRequestActions(
        builder: NotificationCompat.Builder,
        pushNotification: PushNotification
    ) {
        val userId = pushNotification.getUserId() ?: return
        
        // Accept action
        val acceptIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_ACCEPT_FRIEND_REQUEST"
            putExtra(EXTRA_USER_ID, userId)
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_ACCEPT + userId.hashCode(),
            acceptIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        // Decline action
        val declineIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_DECLINE_FRIEND_REQUEST"
            putExtra(EXTRA_USER_ID, userId)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_DECLINE + userId.hashCode(),
            declineIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        builder.addAction(
            android.R.drawable.ic_input_add,
            "Accept",
            acceptPendingIntent
        )
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Decline",
            declinePendingIntent
        )
    }
    
    /**
     * Get appropriate notification icon for type
     */
    private fun getNotificationIcon(type: NotificationType): Int {
        return when (type) {
            NotificationType.FRIEND_REQUEST -> android.R.drawable.ic_menu_add
            NotificationType.FRIEND_REQUEST_ACCEPTED -> android.R.drawable.ic_menu_send
            NotificationType.LOCATION_UPDATE -> android.R.drawable.ic_dialog_map
            NotificationType.FAMILY_CIRCLE -> android.R.drawable.ic_menu_myplaces
            NotificationType.LOW_BATTERY -> android.R.drawable.ic_dialog_alert
            NotificationType.GEOFENCE -> android.R.drawable.ic_menu_compass
            NotificationType.GENERAL -> android.R.drawable.ic_dialog_info
        }
    }
    
    /**
     * Get notification priority for type
     */
    private fun getNotificationPriority(type: NotificationType): Int {
        return when (type.priority) {
            5 -> NotificationCompat.PRIORITY_HIGH
            3 -> NotificationCompat.PRIORITY_DEFAULT
            2 -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
    
    /**
     * Load image from URL for big picture notifications
     */
    private suspend fun loadImageFromUrl(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            connection.getInputStream().use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cancel a notification by ID
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
