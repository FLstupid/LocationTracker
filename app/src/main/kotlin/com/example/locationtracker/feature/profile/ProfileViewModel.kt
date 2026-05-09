package com.example.locationtracker.feature.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.repository.StorageRepository
import com.example.locationtracker.domain.repository.UploadResult
import com.example.locationtracker.domain.usecase.GetCurrentUserUseCase
import com.example.locationtracker.domain.usecase.UpdateDisplayNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val currentUser: User? = null,
    val updateStatus: String? = null,
    val isLoading: Boolean = false,
    val uploadProgress: Int = 0,
    val error: UiError? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase,
    private val storageRepository: StorageRepository,
    private val userRepository: com.example.locationtracker.domain.repository.UserRepository,
    private val settingsRepository: com.example.locationtracker.domain.repository.SettingsRepository,
    private val localDataSource: com.example.locationtracker.data.datasource.LocalDataSource,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        Log.d(TAG, "loadUserData: Starting...")
        viewModelScope.launch {
            getCurrentUserUseCase()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Log.e(TAG, "loadUserData: Error", e)
                    _uiState.update {
                        it.copy(
                            error = UiError.from(e),
                            isLoading = false
                        )
                    }
                }
                .collect { user ->
                    Log.d(TAG, "loadUserData: Received user = ${user?.displayName}")
                    _uiState.update { it.copy(currentUser = user, isLoading = false) }
                }
        }
    }

    fun updateDisplayName(displayName: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateDisplayNameUseCase(displayName)
                _uiState.update {
                    it.copy(
                        updateStatus = "Profile updated successfully!",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        updateStatus = "Error updating profile: ${e.message}",
                        error = UiError.from(e),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearUpdateStatus() {
        _uiState.update { it.copy(updateStatus = null) }
    }
    
    fun updatePhone(phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.updatePhone(phone)
                _uiState.update {
                    it.copy(
                        updateStatus = "Phone number updated successfully",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        updateStatus = "Error updating phone: ${e.message}",
                        error = UiError.from(e),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(uploadProgress = 0) }
            
            storageRepository.uploadProfilePhoto(uri).collect { result ->
                when (result) {
                    is UploadResult.Progress -> {
                        _uiState.update { it.copy(uploadProgress = result.percentage) }
                    }
                    is UploadResult.Success -> {
                        // Update user's photoUrl in Firestore
                        userRepository.updatePhotoUrl(result.downloadUrl)
                        _uiState.update {
                            it.copy(
                                uploadProgress = 100,
                                updateStatus = "Profile photo updated successfully"
                            )
                        }
                    }
                    is UploadResult.Error -> {
                        _uiState.update {
                            it.copy(
                                uploadProgress = 0,
                                updateStatus = "Failed to upload photo: ${result.exception.message}"
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sign out user and clear all local data
     */
    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sign out from Firebase
                firebaseAuth.signOut()
                
                // Clear all local database
                localDataSource.clearAllData()
                
                // Clear all settings and preferences
                settingsRepository.clearAllSettings()
                
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        updateStatus = "Signed out successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        updateStatus = "Error signing out: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }
}
