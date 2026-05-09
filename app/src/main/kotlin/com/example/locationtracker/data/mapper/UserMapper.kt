package com.example.locationtracker.data.mapper

import com.example.locationtracker.data.room.entity.UserEntity
import com.example.locationtracker.domain.enums.PresenceStatus
import com.example.locationtracker.domain.model.User

fun User.toEntity(): UserEntity {
    return UserEntity(
        uid = uid,
        email = email,
        displayName = displayName,
        phone = phone,
        friends = friends,
        sharingWithFriends = sharingWithFriends,
        sharingWithFamily = sharingWithFamily,
        isSharingLocation = isSharingLocation,
        presenceStatus = presenceStatus.name,
        lastSeen = lastSeen,
        batteryLevel = batteryLevel,
        photoUrl = photoUrl,
        fcmToken = fcmToken,
    )
}

fun UserEntity.toModel(): User {
    return User(
        uid = uid,
        email = email ?: "",
        displayName = displayName ?: "",
        phone = phone ?: "",
        friends = friends,
        sharingWithFriends = sharingWithFriends,
        sharingWithFamily = sharingWithFamily,
        isSharingLocation = isSharingLocation,
        presenceStatus = PresenceStatus.valueOf(presenceStatus),
        lastSeen = lastSeen,
        batteryLevel = batteryLevel,
        photoUrl = photoUrl,
        fcmToken = fcmToken,
    )
}
