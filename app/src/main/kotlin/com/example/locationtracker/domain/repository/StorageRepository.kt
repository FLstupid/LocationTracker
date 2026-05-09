package com.example.locationtracker.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing file storage (Firebase Storage)
 */
interface StorageRepository {
    /**
     * Upload a profile photo for the current user
     * @param uri Local file URI
     * @return Flow emitting upload progress (0-100) and final download URL
     */
    suspend fun uploadProfilePhoto(uri: Uri): Flow<UploadResult>

    /**
     * Delete the current user's profile photo
     */
    suspend fun deleteProfilePhoto(): Boolean

    /**
     * Get download URL for a user's profile photo
     * @param userId User ID
     * @return Download URL or null if not found
     */
    suspend fun getProfilePhotoUrl(userId: String): String?
}

/**
 * Result of file upload operation
 */
sealed class UploadResult {
    data class Progress(val percentage: Int) : UploadResult()
    data class Success(val downloadUrl: String) : UploadResult()
    data class Error(val exception: Exception) : UploadResult()
}
