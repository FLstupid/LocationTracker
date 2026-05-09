package com.example.locationtracker.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_locations")
data class LiveLocationEntity(
    @PrimaryKey
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    @ColumnInfo(name = "user_display_name")
    val userDisplayName: String,
    @ColumnInfo(name = "user_email")
    val userEmail: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long = System.currentTimeMillis()
)
