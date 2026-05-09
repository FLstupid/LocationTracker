package com.example.locationtracker.data.mapper

import com.example.locationtracker.data.room.entity.TrackedLocationEntity
import com.example.locationtracker.domain.model.TrackedLocation

fun TrackedLocation.toEntity(): TrackedLocationEntity {
    return TrackedLocationEntity(
        id = id,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

fun TrackedLocationEntity.toModel(): TrackedLocation {
    return TrackedLocation(
        id = id,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}
