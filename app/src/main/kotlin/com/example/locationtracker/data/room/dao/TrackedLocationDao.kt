package com.example.locationtracker.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.locationtracker.data.room.entity.TrackedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedLocation(location: TrackedLocationEntity)

    @Query("SELECT * FROM tracked_locations WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getTrackedLocations(userId: String): Flow<List<TrackedLocationEntity>>

    @Query("DELETE FROM tracked_locations WHERE user_id = :userId")
    suspend fun deleteTrackedLocations(userId: String)

    @Query("SELECT * FROM tracked_locations WHERE user_id = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestTrackedLocation(userId: String): TrackedLocationEntity?
    
    @Query("DELETE FROM tracked_locations")
    suspend fun deleteAllLocations()
}
