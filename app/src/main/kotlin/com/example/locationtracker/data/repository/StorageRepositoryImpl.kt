package com.example.locationtracker.data.repository

import android.net.Uri
import com.example.locationtracker.domain.repository.StorageRepository
import com.example.locationtracker.domain.repository.UploadResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : StorageRepository {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun uploadProfilePhoto(uri: Uri): Flow<UploadResult> = callbackFlow {
        val userId = currentUserId ?: run {
            trySend(UploadResult.Error(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        try {
            val storageRef = storage.reference
                .child("profile_photos")
                .child("$userId.jpg")

            val uploadTask = storageRef.putFile(uri)

            // Listen for upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                trySend(UploadResult.Progress(progress))
            }

            // Wait for upload to complete
            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            trySend(UploadResult.Success(downloadUrl))

        } catch (e: Exception) {
            trySend(UploadResult.Error(e))
        }

        awaitClose()
    }

    override suspend fun deleteProfilePhoto(): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val storageRef = storage.reference
                .child("profile_photos")
                .child("$userId.jpg")

            storageRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProfilePhotoUrl(userId: String): String? {
        return try {
            val storageRef = storage.reference
                .child("profile_photos")
                .child("$userId.jpg")

            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}
