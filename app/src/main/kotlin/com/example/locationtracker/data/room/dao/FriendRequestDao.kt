package com.example.locationtracker.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.locationtracker.data.room.entity.FriendRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(friendRequest: FriendRequestEntity)

    @Query("SELECT * FROM friend_requests WHERE to_uid = :toUid AND status = 'pending'")
    fun getIncomingFriendRequests(toUid: String): Flow<List<FriendRequestEntity>>

    @Query("SELECT * FROM friend_requests WHERE to_uid = :toUid AND status = 'pending'")
    suspend fun getIncomingFriendRequestsList(toUid: String): List<FriendRequestEntity>

    @Query("UPDATE friend_requests SET status = :status WHERE from_uid = :fromUid AND to_uid = :toUid")
    suspend fun updateFriendRequestStatus(fromUid: String, toUid: String, status: String)

    @Query("DELETE FROM friend_requests WHERE id = :requestId")
    suspend fun deleteFriendRequest(requestId: String)

    @Query("DELETE FROM friend_requests")
    suspend fun deleteAllRequests()
}
