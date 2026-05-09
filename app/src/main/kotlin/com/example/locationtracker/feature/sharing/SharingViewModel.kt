package com.example.locationtracker.feature.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.usecase.GetCurrentUserUseCase
import com.example.locationtracker.domain.usecase.GetFamilyUseCase
import com.example.locationtracker.domain.usecase.GetFriendsUseCase
import com.example.locationtracker.domain.usecase.ToggleCircleSharingUseCase
import com.example.locationtracker.domain.usecase.ToggleFriendSharingUseCase
import com.example.locationtracker.domain.usecase.ToggleMasterSharingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SharingUiState(
    val friends: List<User> = emptyList(),
    val familyCircles: List<Family> = emptyList(),
    val currentUserData: User? = null,
    val isMasterSharingEnabled: Boolean = false,
    val statusMessage: String? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null
)

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getFamilyUseCase: GetFamilyUseCase,
    private val toggleFriendSharingUseCase: ToggleFriendSharingUseCase,
    private val toggleCircleSharingUseCase: ToggleCircleSharingUseCase,
    private val toggleMasterSharingUseCase: ToggleMasterSharingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharingUiState())
    val uiState: StateFlow<SharingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            combine(
                getCurrentUserUseCase().flowOn(Dispatchers.IO).catch { e ->
                    _uiState.update { it.copy(error = UiError.from(e), isLoading = false) }
                    emit(null)
                },
                getFriendsUseCase().flowOn(Dispatchers.IO).catch { e ->
                    _uiState.update { it.copy(error = UiError.from(e), isLoading = false) }
                    emit(emptyList())
                },
                getFamilyUseCase().flowOn(Dispatchers.IO).catch { e ->
                    _uiState.update { it.copy(error = UiError.from(e), isLoading = false) }
                    emit(emptyList())
                }
            ) { user, friendsList, circles ->
                _uiState.update {
                    it.copy(
                        currentUserData = user,
                        isMasterSharingEnabled = user?.isSharingLocation ?: false,
                        friends = friendsList,
                        familyCircles = circles,
                        isLoading = false
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Unit)
        }
    }

    fun toggleFriendSharing(friendUid: String, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toggleFriendSharingUseCase(friendUid, enable)
                _uiState.update { it.copy(statusMessage = "Friend sharing preference updated.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error updating friend sharing: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun toggleCircleSharing(circleId: String, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toggleCircleSharingUseCase(circleId, enable)
                _uiState.update { it.copy(statusMessage = "Family circle sharing preference updated.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error updating circle sharing: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun toggleMasterSharing(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toggleMasterSharingUseCase(enable)
                _uiState.update {
                    it.copy(
                        isMasterSharingEnabled = enable,
                        statusMessage = "Live location sharing ${if (enable) "enabled" else "disabled"}."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error updating master sharing preference: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun retryLoadData() {
        loadInitialData()
    }
}
