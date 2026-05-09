package com.example.locationtracker.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tracked_locations")
data class TrackedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Date?
)
