package com.example.locationtracker.domain.model

data class Family(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val ownerUid: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

