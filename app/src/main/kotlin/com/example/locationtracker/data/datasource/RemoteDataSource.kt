package com.example.locationtracker.data.datasource

import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.model.TrackedLocation
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.model.UserPresence
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface RemoteDataSource {
    fun getCurrentUserId(): String
    fun getCurrentUser(): Flow<User?>
    suspend fun getUsers(uids: List<String>): Flow<List<User>>
    suspend fun updateDisplayName(displayName: String)
    suspend fun toggleFriendSharing(friendUid: String, enable: Boolean)
    suspend fun toggleCircleSharing(familyId: String, enable: Boolean)
    suspend fun toggleMasterSharing(enable: Boolean)
    fun getFriends(): Flow<List<User>>
    fun getFamily(): Flow<List<Family>>
    fun getFamilyDetail(familyId: String): Flow<Family>
    suspend fun searchUsers(query: String): List<User>
    suspend fun sendFriendRequest(toUid: String, fromPhone: String)
    suspend fun acceptFriendRequest(request: FriendRequest)
    suspend fun rejectFriendRequest(request: FriendRequest)
    suspend fun createFamily(name: String): Boolean
    fun getIncomingFriendRequests(): Flow<List<FriendRequest>>
    suspend fun signUp(name: String, email: String, password: String, phone: String): Boolean
    suspend fun signIn(email: String, password: String): Boolean

    // Location methods
    suspend fun updateLiveLocation(latitude: Double, longitude: Double)
    fun getLiveLocations(sharedUserIds: List<String>): Flow<Map<String, LiveLocation>>
    fun getLocationHistory(startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>>
    fun getLocationHistoryForUser(userId: String, startDate: Date?, endDate: Date?): Flow<List<TrackedLocation>>
    suspend fun addTrackedLocation(userId: String, latitude: Double, longitude: Double)

    // Presence methods
    fun getUserPresence(userId: String): Flow<UserPresence?>
    fun getUsersPresence(userIds: List<String>): Flow<Map<String, UserPresence>>
    suspend fun updatePresence(presence: UserPresence)

    // Photo methods
    suspend fun updatePhotoUrl(photoUrl: String)

    // Profile update methods
    suspend fun updatePhone(phone: String)

    // FCM token methods
    suspend fun updateFcmToken(token: String)
    suspend fun getFcmToken(userId: String): String?
}
