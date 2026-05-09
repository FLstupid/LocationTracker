package com.example.locationtracker.data.repository

import android.util.Log
import com.example.locationtracker.core.utils.NetworkObserver
import com.example.locationtracker.data.datasource.LocalDataSource
import com.example.locationtracker.data.datasource.RemoteDataSource
import com.example.locationtracker.data.mapper.toEntity
import com.example.locationtracker.data.mapper.toModel
import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.enums.PresenceStatus
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.model.UserPresence
import com.example.locationtracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val networkObserver: NetworkObserver
) : UserRepository {
    
    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        // Emit cached data first
        val userId = remoteDataSource.getCurrentUserId()
        Log.d(TAG, "getCurrentUser: userId = $userId")
        
        val localUser = localDataSource.getUser(userId).first()
        Log.d(TAG, "getCurrentUser: localUser = ${localUser?.displayName}")
        emit(localUser?.toModel())

        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            Log.d(TAG, "getCurrentUser: Fetching from remote...")
            remoteDataSource.getCurrentUser()
                .catch { e ->
                    Log.e(TAG, "Failed to fetch current user from remote", e)
                    // Continue with cached data on error
                }
                .collect { remoteUser ->
                    Log.d(TAG, "getCurrentUser: remoteUser = ${remoteUser?.displayName}")
                    if (remoteUser != null) {
                        localDataSource.insertUser(remoteUser.toEntity())
                        emit(remoteUser)
                    }
                }
        } else {
            Log.d(TAG, "getCurrentUser: Offline, using cached data only")
        }
    }

    override fun getFamilyDetail(familyId: String): Flow<Family> = flow {
        // Emit cached family first
        val cachedFamily = localDataSource.getFamilyById(familyId).first()
        if (cachedFamily != null) {
            emit(cachedFamily.toModel())
        }
        
        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            remoteDataSource.getFamilyDetail(familyId)
                .catch { e ->
                    Log.e(TAG, "Failed to fetch family detail from remote", e)
                    // Fallback to cached data if not emitted yet
                    if (cachedFamily == null) {
                        throw e
                    }
                }
                .collect { remoteFamily ->
                    localDataSource.insertFamily(remoteFamily.toEntity())
                    emit(remoteFamily)
                }
        }
    }

    override suspend fun getUsers(uids: List<String>): Flow<List<User>> = flow {
        // Emit cached users first
        val cachedUsers = localDataSource.getAllUsers().first().filter { it.uid in uids }
        emit(cachedUsers.map { it.toModel() })
        
        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            remoteDataSource.getUsers(uids)
                .catch { e ->
                    Log.e(TAG, "Failed to fetch users from remote", e)
                    // Continue with cached data on error
                }
                .collect { remoteUsers ->
                    remoteUsers.forEach { user ->
                        localDataSource.insertUser(user.toEntity())
                    }
                    emit(remoteUsers)
                }
        }
    }

    override suspend fun updateDisplayName(displayName: String) {
        remoteDataSource.updateDisplayName(displayName)
    }

    override suspend fun toggleFriendSharing(friendUid: String, enable: Boolean) {
        remoteDataSource.toggleFriendSharing(friendUid, enable)
    }

    override suspend fun toggleCircleSharing(circleId: String, enable: Boolean) {
        remoteDataSource.toggleCircleSharing(circleId, enable)
    }

    override suspend fun toggleMasterSharing(enable: Boolean) {
        remoteDataSource.toggleMasterSharing(enable)
    }

    override fun getFriends(): Flow<List<User>> {
        return localDataSource.getAllUsers().map { it.map { userEntity -> userEntity.toModel() } }
    }

    override fun getFamily(): Flow<List<Family>> = flow {
        // Emit cached families first
        val cachedFamilies = localDataSource.getAllFamilies().first()
        emit(cachedFamilies.map { it.toModel() })
        
        // Try to fetch from remote if online
        if (networkObserver.isCurrentlyOnline()) {
            remoteDataSource.getFamily()
                .catch { e ->
                    Log.e(TAG, "Failed to fetch families from remote", e)
                    // Continue with cached data on error
                }
                .collect { remoteFamilies ->
                    remoteFamilies.forEach { family ->
                        localDataSource.insertFamily(family.toEntity())
                    }
                    emit(remoteFamilies.map { it })
                }
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        // Search requires network connection
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Cannot search users while offline")
            return emptyList()
        }
        
        return try {
            remoteDataSource.searchUsers(query)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search users", e)
            emptyList()
        }
    }

    override suspend fun sendFriendRequest(toUid: String, fromPhone: String) {
        if (!networkObserver.isCurrentlyOnline()) {
            throw IllegalStateException("Cannot send friend request while offline")
        }
        remoteDataSource.sendFriendRequest(toUid, fromPhone)
    }

    override suspend fun acceptFriendRequest(request: FriendRequest) {
        if (!networkObserver.isCurrentlyOnline()) {
            throw IllegalStateException("Cannot accept friend request while offline")
        }
        remoteDataSource.acceptFriendRequest(request)
        localDataSource.updateFriendRequestStatus(request.fromUid, request.toUid, "accepted")
    }

    override suspend fun rejectFriendRequest(request: FriendRequest) {
        if (!networkObserver.isCurrentlyOnline()) {
            throw IllegalStateException("Cannot reject friend request while offline")
        }
        remoteDataSource.rejectFriendRequest(request)
        localDataSource.updateFriendRequestStatus(request.fromUid, request.toUid, "rejected")
    }

    override suspend fun createFamily(name: String): Boolean {
        if (!networkObserver.isCurrentlyOnline()) {
            throw IllegalStateException("Cannot create family while offline")
        }
        return remoteDataSource.createFamily(name)
    }

    override fun getIncomingFriendRequests(): Flow<List<FriendRequest>> = flow {
        val localFriendRequests = localDataSource.getIncomingFriendRequests(remoteDataSource.getCurrentUserId()).first()
        emit(localFriendRequests.map { it.toModel() })

        remoteDataSource.getIncomingFriendRequests().collect { remoteFriendRequests ->
            remoteFriendRequests.forEach { remoteFriendRequest ->
                localDataSource.insertFriendRequest(remoteFriendRequest.toEntity())
            }
            emit(localDataSource.getIncomingFriendRequests(remoteDataSource.getCurrentUserId()).first().map { it.toModel() })
        }
    }

    override suspend fun signUp(name: String, email: String, password: String, phone: String): Boolean {
        val success = remoteDataSource.signUp(name, email, password, phone)
        if (success) {
            // Cache the new user locally
            val userId = remoteDataSource.getCurrentUserId()
            val newUser = User(
                uid = userId,
                email = email,
                displayName = name,
                phone = phone,
                photoUrl = null,
                friends = emptyList(),
                sharingWithFriends = emptyList(),
                sharingWithFamily = emptyList(),
                isSharingLocation = false,
                presenceStatus = PresenceStatus.OFFLINE,
                lastSeen = System.currentTimeMillis(),
                batteryLevel = null,
                fcmToken = null
            )
            localDataSource.insertUser(newUser.toEntity())
        }
        return success
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        return remoteDataSource.signIn(email, password)
    }

    override fun getUserPresence(userId: String): Flow<UserPresence?> {
        return remoteDataSource.getUserPresence(userId)
    }

    override fun getUsersPresence(userIds: List<String>): Flow<Map<String, UserPresence>> {
        return remoteDataSource.getUsersPresence(userIds)
    }

    override suspend fun updatePresence(presence: UserPresence) {
        remoteDataSource.updatePresence(presence)
    }

    override suspend fun updatePhotoUrl(photoUrl: String) {
        remoteDataSource.updatePhotoUrl(photoUrl)
    }
    
    override suspend fun updatePhone(phone: String) {
        remoteDataSource.updatePhone(phone)
    }
    
    override suspend fun updateFcmToken(token: String) {
        remoteDataSource.updateFcmToken(token)
    }
    
    override suspend fun getFcmToken(userId: String): String? {
        return remoteDataSource.getFcmToken(userId)
    }
}
