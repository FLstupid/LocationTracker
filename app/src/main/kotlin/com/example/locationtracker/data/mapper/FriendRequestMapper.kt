package com.example.locationtracker.data.mapper

import com.example.locationtracker.data.room.entity.FriendRequestEntity
import com.example.locationtracker.domain.model.FriendRequest

fun FriendRequest.toEntity(): FriendRequestEntity {
    return FriendRequestEntity(
        id = id,
        fromUid = fromUid,
        fromName = fromName,
        toUid = toUid,
        fromPhone = fromPhone,
        status = status
    )
}

fun FriendRequestEntity.toModel(): FriendRequest {
    return FriendRequest(
        id = id,
        fromUid = fromUid,
        fromName = fromName,
        toUid = toUid,
        fromPhone = fromPhone,
        status = status
    )
}
