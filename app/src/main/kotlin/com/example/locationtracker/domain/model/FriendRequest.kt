package com.example.locationtracker.domain.model

data class FriendRequest(
    val id: String = "",  // Format: fromUid_toUid
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val fromPhone: String = "",
    val status: String = "pending" // pending, accepted, rejected
)
