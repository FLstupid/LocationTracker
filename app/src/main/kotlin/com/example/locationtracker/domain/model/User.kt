package com.example.locationtracker.domain.model

import com.example.locationtracker.domain.enums.PresenceStatus

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phone: String = "",
    val friends: List<String> = emptyList(),
    val sharingWithFriends: List<String> = emptyList(),
    val sharingWithFamily: List<String> = emptyList(),
    val isSharingLocation: Boolean = false,
    
    // Presence fields
    val presenceStatus: PresenceStatus = PresenceStatus.OFFLINE,
    val lastSeen: Long = 0L, // Timestamp in milliseconds
    val batteryLevel: Int? = null, // 0-100, null if unknown
    val photoUrl: String? = null, // Profile photo URL (for future use)
    val fcmToken: String? = null // Firebase Cloud Messaging token (for future use)
)

