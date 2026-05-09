package com.example.locationtracker.core.notification

import android.util.Log
import com.example.locationtracker.domain.enums.NotificationType
import com.example.locationtracker.domain.model.PushNotificationBuilder
import com.example.locationtracker.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * Receives notifications when the app is in foreground or background.
 */
@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "NotificationService"
        
        // Notification data keys
        private const val KEY_TYPE = "type"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_USER_ID = "userId"
        private const val KEY_CIRCLE_ID = "circleId"
        private const val KEY_PLACE_NAME = "placeName"
        private const val KEY_IMAGE_URL = "imageUrl"
        private const val KEY_DEEP_LINK = "deepLink"
    }
    
    /**
     * Called when a new FCM token is generated.
     * Save it to Firestore for the current user.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        
        // Save token to Firestore
        serviceScope.launch {
            try {
                userRepository.updateFcmToken(token)
                Log.d(TAG, "FCM token saved to Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save FCM token", e)
            }
        }
    }
    
    /**
     * Called when a message is received.
     * Parse the message and show a notification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification: ${notification.title}")
            handleNotificationMessage(notification, remoteMessage.data)
        }
    }
    
    /**
     * Handle data-only messages (background or foreground)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data[KEY_TYPE]?.let { 
            NotificationType.fromString(it) 
        } ?: NotificationType.GENERAL
        
        val title = data[KEY_TITLE] ?: "New Notification"
        val message = data[KEY_MESSAGE] ?: ""
        val imageUrl = data[KEY_IMAGE_URL]
        
        // Build notification data map
        val notificationData = mutableMapOf<String, String>()
        data[KEY_USER_ID]?.let { notificationData["userId"] = it }
        data[KEY_CIRCLE_ID]?.let { notificationData["circleId"] = it }
        data[KEY_PLACE_NAME]?.let { notificationData["placeName"] = it }
        data[KEY_DEEP_LINK]?.let { notificationData["deepLink"] = it }
        
        // Create push notification object
        val pushNotification = PushNotificationBuilder()
            .id(System.currentTimeMillis().toString())
            .title(title)
            .message(message)
            .type(notificationType)
            .data(notificationData)
            .timestamp(Date())
            .imageUrl(imageUrl)
            .build()
        
        // Show notification
        notificationHelper.showNotification(pushNotification)
    }
    
    /**
     * Handle notification messages (only received in foreground)
     */
    private fun handleNotificationMessage(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        val notificationType = data[KEY_TYPE]?.let { 
            NotificationType.fromString(it) 
        } ?: NotificationType.GENERAL
        
        // Build notification data map
        val notificationData = mutableMapOf<String, String>()
        data[KEY_USER_ID]?.let { notificationData["userId"] = it }
        data[KEY_CIRCLE_ID]?.let { notificationData["circleId"] = it }
        data[KEY_PLACE_NAME]?.let { notificationData["placeName"] = it }
        data[KEY_DEEP_LINK]?.let { notificationData["deepLink"] = it }
        
        // Create push notification object
        val pushNotification = PushNotificationBuilder()
            .id(System.currentTimeMillis().toString())
            .title(notification.title ?: "New Notification")
            .message(notification.body ?: "")
            .type(notificationType)
            .data(notificationData)
            .timestamp(Date())
            .imageUrl(notification.imageUrl?.toString())
            .build()
        
        // Show notification
        notificationHelper.showNotification(pushNotification)
    }
    
    /**
     * Called when messages are deleted on the server
     */
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "Messages deleted on server")
        // Optional: Sync with server to recover missed messages
        // This is handled automatically by FCM in most cases
        // Custom sync can be implemented here if needed
    }
}
