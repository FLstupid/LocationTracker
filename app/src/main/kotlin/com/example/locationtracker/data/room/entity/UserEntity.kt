package com.example.locationtracker.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,

    val email: String?,

    @ColumnInfo(name = "display_name")
    val displayName: String?,

    @ColumnInfo(name = "phone")
    val phone: String?,

    val friends: List<String>,

    @ColumnInfo(name = "sharing_with_friends")
    val sharingWithFriends: List<String>,

    @ColumnInfo(name = "sharing_with_family")
    val sharingWithFamily: List<String>,

    @ColumnInfo(name = "is_sharing_location")
    val isSharingLocation: Boolean,

    @ColumnInfo(name = "presence_status")
    val presenceStatus: String, // Store enum as String

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long,

    @ColumnInfo(name = "battery_level")
    val batteryLevel: Int?,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String?,

    @ColumnInfo(name = "fcm_token")
    val fcmToken: String?
)
