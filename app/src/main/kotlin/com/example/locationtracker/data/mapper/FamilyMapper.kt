package com.example.locationtracker.data.mapper

import com.example.locationtracker.data.room.entity.FamilyEntity
import com.example.locationtracker.domain.model.Family

fun FamilyEntity.toModel(): Family {
    return Family(
        id = id,
        name = name,
        members = if (members.isBlank()) emptyList() else members.split(","),
        ownerUid = ownerUid,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Family.toEntity(): FamilyEntity {
    return FamilyEntity(
        id = id,
        name = name,
        members = members.joinToString(","),
        ownerUid = ownerUid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = System.currentTimeMillis()
    )
}
