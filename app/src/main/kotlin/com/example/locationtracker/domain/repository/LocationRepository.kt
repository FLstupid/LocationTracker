package com.example.locationtracker.domain.repository

import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.model.TrackedLocation
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface LocationRepository {
    suspend fun updateLiveLocation(latitude: Double, longitude: Double)
    fun getLiveLocations(sharedUserIds: List<String>): Flow<Map<String, LiveLocation>>
    fun getLocationHistory(startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>>
    fun getLocationHistoryForUser(userId: String, startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>>
    suspend fun addTrackedLocation(latitude: Double, longitude: Double)
}
