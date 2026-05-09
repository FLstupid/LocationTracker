package com.example.locationtracker.domain.repository

import com.example.locationtracker.domain.model.Activity
import com.example.locationtracker.domain.enums.ActivityType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing activity feed data.
 * Handles fetching, creating, and managing user activities.
 */
interface ActivityRepository {
    /**
     * Get activity feed for the current user
     * Returns activities from friends and family members
     * 
     * @param limit Number of activities to fetch per page
     * @param lastActivityId ID of the last activity for pagination (null for first page)
     * @return Flow of activity list
     */
    fun getActivityFeed(limit: Int = 20, lastActivityId: String? = null): Flow<List<Activity>>
    
    /**
     * Get filtered activities by type
     * 
     * @param types List of activity types to filter by
     * @param limit Number of activities to fetch
     * @return Flow of filtered activities
     */
    fun getActivitiesByType(types: List<ActivityType>, limit: Int = 20): Flow<List<Activity>>
    
    /**
     * Get activities for a specific user
     * 
     * @param userId User ID to get activities for
     * @param limit Number of activities to fetch
     * @return Flow of user's activities
     */
    fun getUserActivities(userId: String, limit: Int = 20): Flow<List<Activity>>
    
    /**
     * Create a new activity
     * 
     * @param userId User performing the activity
     * @param type Type of activity
     * @param data Additional activity data
     */
    suspend fun createActivity(
        userId: String,
        type: ActivityType,
        data: Map<String, String> = emptyMap()
    )
    
    /**
     * Mark activity as read
     * 
     * @param activityId ID of the activity to mark as read
     */
    suspend fun markAsRead(activityId: String)
    
    /**
     * Mark all activities as read
     */
    suspend fun markAllAsRead()
    
    /**
     * Get count of unread activities
     * 
     * @return Flow of unread count
     */
    fun getUnreadCount(): Flow<Int>
    
    /**
     * Delete an activity
     * 
     * @param activityId ID of the activity to delete
     */
    suspend fun deleteActivity(activityId: String)
    
    /**
     * Clear all activities
     */
    suspend fun clearAllActivities()
}
