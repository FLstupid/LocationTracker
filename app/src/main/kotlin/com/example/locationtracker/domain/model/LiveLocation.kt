package com.example.locationtracker.domain.model

data class LiveLocation(
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val geohash: String? = null
)
