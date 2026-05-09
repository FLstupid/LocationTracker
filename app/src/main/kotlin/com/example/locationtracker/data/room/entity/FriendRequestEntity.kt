package com.example.locationtracker.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_requests")
data class FriendRequestEntity(
    @PrimaryKey
    val id: String,  // Format: fromUid_toUid
    @ColumnInfo(name = "from_uid")
    val fromUid: String,
    @ColumnInfo(name = "from_name")
    val fromName: String,
    @ColumnInfo(name = "from_phone")
    val fromPhone: String,
    @ColumnInfo(name = "to_uid")
    val toUid: String,
    val status: String
)
