package com.example.locationtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.locationtracker.core.notification.NotificationChannelManager
import com.example.locationtracker.core.service.PresenceService
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LocationTrackerApp : Application() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    @Inject
    lateinit var presenceService: PresenceService

    override fun onCreate() {
        super.onCreate()

        // Move ALL channel creation to background thread
        Thread {
            // Create legacy location notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "location",
                    "Location",
                    NotificationManager.IMPORTANCE_LOW
                )
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Create notification channels for push notifications
            notificationChannelManager.createNotificationChannels()
            Log.d("LocationTrackerApp", "Notification channels created")
        }.start()

        // Register presence service (lightweight)
        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceService)

        // Move heavy initialization to background thread
        Thread {
            // Request FCM token in background
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("LocationTrackerApp", "FCM token: $token")
                    // Token will be saved automatically by NotificationService.onNewToken()
                } else {
                    Log.e("LocationTrackerApp", "Failed to get FCM token", task.exception)
                }
            }
        }.start()
    }
}
