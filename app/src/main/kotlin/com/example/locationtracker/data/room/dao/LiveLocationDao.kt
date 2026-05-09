package com.example.locationtracker.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.locationtracker.data.room.entity.LiveLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveLocationDao {
    @Query("SELECT * FROM live_locations ORDER BY timestamp DESC")
    fun getAllLiveLocations(): Flow<List<LiveLocationEntity>>

    @Query("SELECT * FROM live_locations WHERE userId = :userId")
    fun getLiveLocationByUserId(userId: String): Flow<LiveLocationEntity?>

    @Query("SELECT * FROM live_locations WHERE userId IN (:userIds)")
    fun getLiveLocationsByUserIds(userIds: List<String>): Flow<List<LiveLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLiveLocation(location: LiveLocationEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLiveLocations(locations: List<LiveLocationEntity>)

    @Query("DELETE FROM live_locations WHERE userId = :userId")
    suspend fun deleteLiveLocation(userId: String)

    @Query("DELETE FROM live_locations")
    suspend fun deleteAllLiveLocations()

    @Query("DELETE FROM live_locations WHERE timestamp < :timestamp")
    suspend fun deleteOldLocations(timestamp: Long)

    @Query("DELETE FROM live_locations")
    suspend fun deleteAllLocations()
}