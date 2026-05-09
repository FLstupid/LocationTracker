package com.example.locationtracker.data.repository

import android.util.Log
import com.example.locationtracker.core.utils.NetworkObserver
import com.example.locationtracker.data.datasource.LocalDataSource
import com.example.locationtracker.data.datasource.RemoteDataSource
import com.example.locationtracker.data.mapper.toEntity
import com.example.locationtracker.data.mapper.toModel
import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.model.TrackedLocation
import com.example.locationtracker.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val networkObserver: NetworkObserver
) : LocationRepository {

    companion object {
        private const val TAG = "LocationRepositoryImpl"
    }

    override suspend fun updateLiveLocation(latitude: Double, longitude: Double) {
        val userId = remoteDataSource.getCurrentUserId()
        if (userId.isBlank()) return

        val nowMillis = System.currentTimeMillis()
        val liveLocation = LiveLocation(
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            timestamp = nowMillis
        )

        // Persist live and history locally first to avoid data loss when offline.
        localDataSource.insertLiveLocation(liveLocation.toEntity())
        localDataSource.insertTrackedLocation(
            TrackedLocation(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                timestamp = Date(nowMillis)
            ).toEntity()
        )

        // Try to update remote if online
        if (networkObserver.isCurrentlyOnline()) {
            try {
                remoteDataSource.updateLiveLocation(latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update live location to remote", e)
            }
        } else {
            Log.w(TAG, "Offline - live location will be updated when online")
        }
    }

    override fun getLiveLocations(sharedUserIds: List<String>): Flow<Map<String, LiveLocation>> = flow {
        // Emit cached live locations first
        val cachedLocations = localDataSource.getAllLiveLocations().first()
        val cachedMap = cachedLocations
            .filter { it.userId in sharedUserIds }
            .associate { it.userId to it.toModel() }
        emit(cachedMap)

        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            try {
                remoteDataSource.getLiveLocations(sharedUserIds).collect { remoteLocations ->
                    localDataSource.insertLiveLocations(
                        remoteLocations.values.map { liveLocation -> liveLocation.toEntity() }
                    )
                    emit(remoteLocations)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch live locations from remote", e)
                // Keep showing cached data
            }
        }
    }

    override fun getLocationHistory(startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>> = flow {
        // Emit cached history first
        val userId = remoteDataSource.getCurrentUserId()
        val localHistory = localDataSource.getTrackedLocations(userId).first()
        emit(localHistory.map { it.toModel() })

        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            try {
                val remoteHistory = remoteDataSource.getLocationHistory(startDate, endDate).first()
                remoteHistory.forEach {
                    localDataSource.insertTrackedLocation(it.toEntity())
                }
                emit(localDataSource.getTrackedLocations(userId).first().map { it.toModel() })
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch location history from remote", e)
                // Keep showing cached data
            }
        }
    }

    override fun getLocationHistoryForUser(userId: String, startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>> = flow {
        // Check if current user has permission to view this user's location history
        val currentUserId = remoteDataSource.getCurrentUserId()

        // For demo purposes, we'll allow viewing history of users who are sharing live location
        // In a production app, you'd check specific sharing permissions for historical data

        // Emit cached history first (if available locally)
        val localHistory = localDataSource.getTrackedLocations(userId).first()
        val filteredLocalHistory = localHistory
            .map { it.toModel() }
            .filter { location ->
                (startDate == null || location.timestamp == null || location.timestamp >= startDate) &&
                (endDate == null || location.timestamp == null || location.timestamp <= endDate)
            }
        emit(filteredLocalHistory)

        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            try {
                val remoteHistory = remoteDataSource.getLocationHistoryForUser(userId, startDate, endDate).first()

                // Cache the fetched history
                remoteHistory.forEach {
                    localDataSource.insertTrackedLocation(it.toEntity())
                }

                // Emit updated data
                val updatedHistory = localDataSource.getTrackedLocations(userId).first()
                    .map { it.toModel() }
                    .filter { location ->
                        (startDate == null || location.timestamp == null || location.timestamp >= startDate) &&
                        (endDate == null || location.timestamp == null || location.timestamp <= endDate)
                    }
                emit(updatedHistory)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch location history for user $userId from remote", e)
                // Keep showing cached data
                if (filteredLocalHistory.isEmpty()) {
                    // Note: We can't set error message here as it's not available in this context
                    // The ViewModel will handle empty results appropriately
                }
            }
        } else {
            Log.w(TAG, "Offline - showing cached location history for user $userId")
        }
    }

    override suspend fun addTrackedLocation(latitude: Double, longitude: Double) {
        val userId = remoteDataSource.getCurrentUserId()
        if (userId.isNotBlank()) {
            // Cache location locally first
            val location = TrackedLocation(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                timestamp = Date()
            )
            localDataSource.insertTrackedLocation(location.toEntity())

            // Try to sync to remote if online
            if (networkObserver.isCurrentlyOnline()) {
                try {
                    remoteDataSource.addTrackedLocation(userId, latitude, longitude)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add tracked location to remote", e)
                    // Location is cached, will sync later
                }
            } else {
                Log.w(TAG, "Offline - tracked location cached for later sync")
            }
        }
    }
}