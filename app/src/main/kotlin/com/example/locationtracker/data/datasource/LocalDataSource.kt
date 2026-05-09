package com.example.locationtracker.data.datasource

import com.example.locationtracker.data.room.entity.FamilyEntity
import com.example.locationtracker.data.room.entity.FriendRequestEntity
import com.example.locationtracker.data.room.entity.LiveLocationEntity
import com.example.locationtracker.data.room.entity.TrackedLocationEntity
import com.example.locationtracker.data.room.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    // User operations
    suspend fun insertUser(user: UserEntity)
    suspend fun insertUsers(users: List<UserEntity>)
    fun getUser(uid: String): Flow<UserEntity?>
    fun getAllUsers(): Flow<List<UserEntity>>

    // Location operations
    suspend fun insertTrackedLocation(location: TrackedLocationEntity)
    fun getTrackedLocations(userId: String): Flow<List<TrackedLocationEntity>>
    suspend fun getLatestTrackedLocation(userId: String): TrackedLocationEntity?
    suspend fun deleteTrackedLocations(userId: String)

    // Friend request operations
    suspend fun insertFriendRequest(friendRequest: FriendRequestEntity)
    fun getIncomingFriendRequests(toUid: String): Flow<List<FriendRequestEntity>>
    suspend fun updateFriendRequestStatus(fromUid: String, toUid: String, status: String)
    suspend fun deleteFriendRequest(requestId: String)
    
    // Family operations
    suspend fun insertFamily(family: FamilyEntity)
    suspend fun insertFamilies(families: List<FamilyEntity>)
    fun getAllFamilies(): Flow<List<FamilyEntity>>
    fun getFamilyById(familyId: String): Flow<FamilyEntity?>
    suspend fun deleteFamily(familyId: String)
    
    // Live location operations
    suspend fun insertLiveLocation(location: LiveLocationEntity)
    suspend fun insertLiveLocations(locations: List<LiveLocationEntity>)
    fun getAllLiveLocations(): Flow<List<LiveLocationEntity>>
    fun getLiveLocationByUserId(userId: String): Flow<LiveLocationEntity?>
    suspend fun deleteOldLiveLocations(timestamp: Long)
    
    // Clear all local data (for sign out)
    suspend fun clearAllData()
}
