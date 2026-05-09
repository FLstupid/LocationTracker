package com.example.locationtracker.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.locationtracker.domain.enums.PresenceStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to track and update user presence in Firestore
 * Monitors app lifecycle and battery level
 */
@Singleton
class PresenceService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : DefaultLifecycleObserver {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var batteryReceiver: BroadcastReceiver? = null
    private var currentBatteryLevel: Int? = null

    init {
        registerBatteryReceiver()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        updatePresence(PresenceStatus.ONLINE)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        updatePresence(PresenceStatus.OFFLINE)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unregisterBatteryReceiver()
        serviceScope.cancel()
    }

    /**
     * Update user's presence status in Firestore
     */
    private fun updatePresence(status: PresenceStatus) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                val presenceData = hashMapOf(
                    "status" to status.name,
                    "lastSeen" to System.currentTimeMillis(),
                    "batteryLevel" to currentBatteryLevel,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("users")
                    .document(userId)
                    .update("presence", presenceData)
                    .addOnFailureListener {
                        // If update fails (document doesn't exist), try to set it
                        firestore.collection("users")
                            .document(userId)
                            .set(mapOf("presence" to presenceData), com.google.firebase.firestore.SetOptions.merge())
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Manually update presence (called when user performs an action)
     */
    fun updateActivity() {
        updatePresence(PresenceStatus.ONLINE)
    }

    /**
     * Update battery level in presence
     */
    fun updateBatteryLevel(level: Int) {
        currentBatteryLevel = level
        val userId = firebaseAuth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("presence.batteryLevel", level)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update current location/place name in presence
     */
    fun updateCurrentPlace(placeName: String?) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("presence.currentPlace", placeName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update location sharing status in presence
     */
    fun updateSharingStatus(isSharing: Boolean) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("presence.isSharing", isSharing)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        val batteryPct = (level / scale.toFloat() * 100).toInt()
                        if (batteryPct != currentBatteryLevel) {
                            currentBatteryLevel = batteryPct
                            updateBatteryLevel(batteryPct)
                        }
                    }
                }
            }
        }

        context.registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered
            }
        }
        batteryReceiver = null
    }
}
