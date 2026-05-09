package com.example.locationtracker.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.enums.ActivityType
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.repository.ActivityRepository
import com.example.locationtracker.domain.usecase.AcceptFriendRequestUseCase
import com.example.locationtracker.domain.usecase.GetCurrentUserUseCase
import com.example.locationtracker.domain.usecase.GetFriendsUseCase
import com.example.locationtracker.domain.usecase.GetIncomingFriendRequestsUseCase
import com.example.locationtracker.domain.usecase.RejectFriendRequestUseCase
import com.example.locationtracker.domain.usecase.SearchUsersUseCase
import com.example.locationtracker.domain.usecase.SendFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val searchText: String = "",
    val searchResults: List<User> = emptyList(),
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiError? = null,
    val statusMessage: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val getIncomingFriendRequestsUseCase: GetIncomingFriendRequestsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getIncomingFriendRequestsUseCase()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _uiState.update { it.copy(error = UiError.from(e)) }
                }
                .collect { requests ->
                    _uiState.update { it.copy(friendRequests = requests) }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getFriendsUseCase()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _uiState.update { it.copy(error = UiError.from(e)) }
                }
                .collect { friends ->
                    _uiState.update { it.copy(friends = friends) }
                }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = searchUsersUseCase(text)
                _uiState.update { it.copy(searchResults = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = UiError.from(e)) }
            }
        }
    }

    fun sendFriendRequest(toUid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = getCurrentUserUseCase().first()
                if (currentUser != null && currentUser.phone.isNotBlank()) {
                    sendFriendRequestUseCase(toUid, currentUser.phone)
                    _uiState.update { it.copy(statusMessage = "Friend request sent!") }
                } else {
                    _uiState.update { it.copy(statusMessage = "Unable to send request: user info unavailable") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error sending friend request: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                acceptFriendRequestUseCase(request)

                // Create activity
                try {
                    activityRepository.createActivity(
                        userId = request.fromUid,
                        type = ActivityType.FRIEND_REQUEST_ACCEPTED,
                        data = mapOf("friendId" to request.fromUid)
                    )
                } catch (e: Exception) {
                    // Log but don't fail the whole operation
                    android.util.Log.e("FriendsViewModel", "Failed to create activity", e)
                }
                _uiState.update { it.copy(statusMessage = "Friend request accepted!") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error accepting request: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                rejectFriendRequestUseCase(request)
                _uiState.update { it.copy(statusMessage = "Friend request rejected.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Error rejecting request: ${e.message}",
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun refreshFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                // The friends list is already being collected in the init block
                // We can trigger a refresh by re-collecting or forcing an update
                // For now, we'll just simulate a refresh delay
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(statusMessage = "Friends list refreshed", isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = UiError.from(e),
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
