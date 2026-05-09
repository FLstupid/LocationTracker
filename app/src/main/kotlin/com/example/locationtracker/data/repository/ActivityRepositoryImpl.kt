package com.example.locationtracker.data.repository

import android.util.Log
import com.example.locationtracker.core.utils.NetworkObserver
import com.example.locationtracker.domain.model.Activity
import com.example.locationtracker.domain.enums.ActivityType
import com.example.locationtracker.domain.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkObserver: NetworkObserver
) : ActivityRepository {
    
    companion object {
        private const val TAG = "ActivityRepositoryImpl"
        private const val COLLECTION_ACTIVITIES = "activities"
    }

    override fun getActivityFeed(limit: Int, lastActivityId: String?): Flow<List<Activity>> = callbackFlow {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot fetch activities")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        var query = firestore.collection(COLLECTION_ACTIVITIES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
        
        // Pagination: start after last activity if provided
        if (lastActivityId != null) {
            try {
                val lastDoc = firestore.collection(COLLECTION_ACTIVITIES)
                    .document(lastActivityId)
                    .get()
                    .await()
                query = query.startAfter(lastDoc)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get last document for pagination", e)
            }
        }
        
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to activities", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                val activities = snapshot.documents.mapNotNull { doc ->
                    try {
                        Activity(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Unknown",
                            userPhotoUrl = doc.getString("userPhotoUrl"),
                            type = ActivityType.fromString(doc.getString("type") ?: ""),
                            timestamp = doc.getDate("timestamp") ?: Date(),
                            data = (doc.get("data") as? Map<*, *>)
                                ?.mapKeys { it.key.toString() }
                                ?.mapValues { it.value.toString() }
                                ?: emptyMap(),
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing activity", e)
                        null
                    }
                }
                trySend(activities)
            }
        }
        
        awaitClose { registration.remove() }
    }
    
    override fun getActivitiesByType(types: List<ActivityType>, limit: Int): Flow<List<Activity>> = callbackFlow {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot fetch activities")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val typeNames = types.map { it.name }
        
        val registration = firestore.collection(COLLECTION_ACTIVITIES)
            .whereIn("type", typeNames)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to filtered activities", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        try {
                            Activity(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "Unknown",
                                userPhotoUrl = doc.getString("userPhotoUrl"),
                                type = ActivityType.fromString(doc.getString("type") ?: ""),
                                timestamp = doc.getDate("timestamp") ?: Date(),
                                data = (doc.get("data") as? Map<*, *>)
                                    ?.mapKeys { it.key.toString() }
                                    ?.mapValues { it.value.toString() }
                                    ?: emptyMap(),
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing activity", e)
                            null
                        }
                    }
                    trySend(activities)
                }
            }
        
        awaitClose { registration.remove() }
    }
    
    override fun getUserActivities(userId: String, limit: Int): Flow<List<Activity>> = callbackFlow {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot fetch activities")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val registration = firestore.collection(COLLECTION_ACTIVITIES)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user activities", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        try {
                            Activity(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "Unknown",
                                userPhotoUrl = doc.getString("userPhotoUrl"),
                                type = ActivityType.fromString(doc.getString("type") ?: ""),
                                timestamp = doc.getDate("timestamp") ?: Date(),
                                data = (doc.get("data") as? Map<*, *>)
                                    ?.mapKeys { it.key.toString() }
                                    ?.mapValues { it.value.toString() }
                                    ?: emptyMap(),
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing activity", e)
                            null
                        }
                    }
                    trySend(activities)
                }
            }
        
        awaitClose { registration.remove() }
    }
    
    override suspend fun createActivity(
        userId: String,
        type: ActivityType,
        data: Map<String, String>
    ) {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot create activity")
            return
        }
        
        try {
            // Get user info
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val userName = userDoc.getString("displayName") ?: "Unknown"
            val userPhotoUrl = userDoc.getString("photoUrl")
            
            val activityData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "userPhotoUrl" to userPhotoUrl,
                "type" to type.name,
                "timestamp" to Date(),
                "data" to data,
                "isRead" to false
            )
            
            firestore.collection(COLLECTION_ACTIVITIES)
                .add(activityData)
                .await()
            
            Log.d(TAG, "Activity created: ${type.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create activity", e)
        }
    }
    
    override suspend fun markAsRead(activityId: String) {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot mark activity as read")
            return
        }
        
        try {
            firestore.collection(COLLECTION_ACTIVITIES)
                .document(activityId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark activity as read", e)
        }
    }
    
    override suspend fun markAllAsRead() {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot mark all activities as read")
            return
        }
        
        try {
            val unreadActivities = firestore.collection(COLLECTION_ACTIVITIES)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            unreadActivities.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            Log.d(TAG, "Marked ${unreadActivities.size()} activities as read")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark all activities as read", e)
        }
    }
    
    override fun getUnreadCount(): Flow<Int> = callbackFlow {
        if (!networkObserver.isCurrentlyOnline()) {
            trySend(0)
            close()
            return@callbackFlow
        }
        
        val registration = firestore.collection(COLLECTION_ACTIVITIES)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to unread count", error)
                    trySend(0)
                    return@addSnapshotListener
                }
                
                trySend(snapshot?.size() ?: 0)
            }
        
        awaitClose { registration.remove() }
    }
    
    override suspend fun deleteActivity(activityId: String) {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot delete activity")
            return
        }
        
        try {
            firestore.collection(COLLECTION_ACTIVITIES)
                .document(activityId)
                .delete()
                .await()
            
            Log.d(TAG, "Activity deleted: $activityId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete activity", e)
        }
    }
    
    override suspend fun clearAllActivities() {
        if (!networkObserver.isCurrentlyOnline()) {
            Log.w(TAG, "Offline - cannot clear activities")
            return
        }
        
        try {
            val activities = firestore.collection(COLLECTION_ACTIVITIES)
                .get()
                .await()
            
            val batch = firestore.batch()
            activities.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Log.d(TAG, "Cleared ${activities.size()} activities")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear activities", e)
        }
    }
}
