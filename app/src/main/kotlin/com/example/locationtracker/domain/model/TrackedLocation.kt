package com.example.locationtracker.domain.model

import java.util.Date

data class TrackedLocation(
    val id: Long = 0,
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Date? = null
)
