package com.example.locationtracker.domain.repository

import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.model.UserPresence
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    fun getFamilyDetail(familyId: String): Flow<Family>
    suspend fun getUsers(uids: List<String>): Flow<List<User>>
    suspend fun updateDisplayName(displayName: String)
    suspend fun toggleFriendSharing(friendUid: String, enable: Boolean)
    suspend fun toggleCircleSharing(circleId: String, enable: Boolean)
    suspend fun toggleMasterSharing(enable: Boolean)
    fun getFriends(): Flow<List<User>>
    fun getFamily(): Flow<List<Family>>
    suspend fun searchUsers(query: String): List<User>
    suspend fun sendFriendRequest(toUid: String, fromPhone: String)
    suspend fun acceptFriendRequest(request: FriendRequest)
    suspend fun rejectFriendRequest(request: FriendRequest)
    suspend fun createFamily(name: String): Boolean
    fun getIncomingFriendRequests(): Flow<List<FriendRequest>>
    suspend fun signUp(name: String, email: String, password: String, phone: String): Boolean
    suspend fun signIn(email: String, password: String): Boolean
    
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
