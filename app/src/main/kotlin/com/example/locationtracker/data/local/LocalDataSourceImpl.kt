package com.example.locationtracker.data.local

import com.example.locationtracker.data.datasource.LocalDataSource
import com.example.locationtracker.data.room.dao.FamilyDao
import com.example.locationtracker.data.room.dao.FriendRequestDao
import com.example.locationtracker.data.room.dao.LiveLocationDao
import com.example.locationtracker.data.room.dao.TrackedLocationDao
import com.example.locationtracker.data.room.dao.UserDao
import com.example.locationtracker.data.room.entity.FamilyEntity
import com.example.locationtracker.data.room.entity.FriendRequestEntity
import com.example.locationtracker.data.room.entity.LiveLocationEntity
import com.example.locationtracker.data.room.entity.TrackedLocationEntity
import com.example.locationtracker.data.room.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val trackedLocationDao: TrackedLocationDao,
    private val userDao: UserDao,
    private val friendRequestDao: FriendRequestDao,
    private val familyDao: FamilyDao,
    private val liveLocationDao: LiveLocationDao
) : LocalDataSource {

    // User operations
    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun insertUsers(users: List<UserEntity>) {
        userDao.insertUsers(users)
    }

    override fun getUser(uid: String): Flow<UserEntity?> {
        return userDao.getUser(uid)
    }

    override fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    // Location operations
    override suspend fun insertTrackedLocation(location: TrackedLocationEntity) {
        trackedLocationDao.insertTrackedLocation(location)
    }

    override fun getTrackedLocations(userId: String): Flow<List<TrackedLocationEntity>> {
        return trackedLocationDao.getTrackedLocations(userId)
    }

    override suspend fun getLatestTrackedLocation(userId: String): TrackedLocationEntity? {
        return trackedLocationDao.getLatestTrackedLocation(userId)
    }

    override suspend fun deleteTrackedLocations(userId: String) {
        trackedLocationDao.deleteTrackedLocations(userId)
    }

    // Friend request operations
    override suspend fun insertFriendRequest(friendRequest: FriendRequestEntity) {
        friendRequestDao.insertFriendRequest(friendRequest)
    }

    override fun getIncomingFriendRequests(toUid: String): Flow<List<FriendRequestEntity>> {
        return friendRequestDao.getIncomingFriendRequests(toUid)
    }

    override suspend fun updateFriendRequestStatus(fromUid: String, toUid: String, status: String) {
        friendRequestDao.updateFriendRequestStatus(fromUid, toUid, status)
    }
    
    override suspend fun deleteFriendRequest(requestId: String) {
        friendRequestDao.deleteFriendRequest(requestId)
    }
    
    // Family operations
    override suspend fun insertFamily(family: FamilyEntity) {
        familyDao.insertFamily(family)
    }

    override suspend fun insertFamilies(families: List<FamilyEntity>) {
        familyDao.insertFamilies(families)
    }

    override fun getAllFamilies(): Flow<List<FamilyEntity>> {
        return familyDao.getAllFamilies()
    }

    override fun getFamilyById(familyId: String): Flow<FamilyEntity?> {
        return familyDao.getFamilyById(familyId)
    }

    override suspend fun deleteFamily(familyId: String) {
        familyDao.deleteFamily(familyId)
    }
    
    // Live location operations
    override suspend fun insertLiveLocation(location: LiveLocationEntity) {
        liveLocationDao.insertLiveLocation(location)
    }

    override suspend fun insertLiveLocations(locations: List<LiveLocationEntity>) {
        liveLocationDao.insertLiveLocations(locations)
    }

    override fun getAllLiveLocations(): Flow<List<LiveLocationEntity>> {
        return liveLocationDao.getAllLiveLocations()
    }

    override fun getLiveLocationByUserId(userId: String): Flow<LiveLocationEntity?> {
        return liveLocationDao.getLiveLocationByUserId(userId)
    }

    override suspend fun deleteOldLiveLocations(timestamp: Long) {
        liveLocationDao.deleteOldLocations(timestamp)
    }
    
    override suspend fun clearAllData() {
        // Clear all tables
        userDao.deleteAllUsers()
        trackedLocationDao.deleteAllLocations()
        friendRequestDao.deleteAllRequests()
        familyDao.deleteAllFamilies()
        liveLocationDao.deleteAllLocations()
    }
}