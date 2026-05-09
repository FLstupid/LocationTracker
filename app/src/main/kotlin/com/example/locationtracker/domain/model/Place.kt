package com.example.locationtracker.domain.model

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class Place(
    val name: String,
    val address: String,
    val latLng: LatLng,
    val id: String
)
