package com.example.locationtracker.data.mapper

import com.example.locationtracker.data.room.entity.LiveLocationEntity
import com.example.locationtracker.domain.model.LiveLocation

fun LiveLocationEntity.toModel(): LiveLocation {
    return LiveLocation(
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        geohash = null
    )
}

fun LiveLocation.toEntity(userDisplayName: String = "", userEmail: String = ""): LiveLocationEntity {
    return LiveLocationEntity(
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        userDisplayName = userDisplayName,
        userEmail = userEmail
    )
}
