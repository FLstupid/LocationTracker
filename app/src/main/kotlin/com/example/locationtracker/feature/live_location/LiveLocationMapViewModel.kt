package com.example.locationtracker.feature.live_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.utils.NetworkObserver
import com.example.locationtracker.core.utils.NetworkStatus
import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.model.Place
import com.example.locationtracker.domain.model.TrackedLocation
import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.usecase.GetAutocompletePredictionsUseCase
import com.example.locationtracker.domain.usecase.GetCurrentUserUseCase
import com.example.locationtracker.domain.usecase.GetFamilyUseCase
import com.example.locationtracker.domain.usecase.GetLiveLocationUseCase
import com.example.locationtracker.domain.usecase.GetUsersUseCase
import com.example.locationtracker.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class UserLiveLocation(val user: User, val location: LiveLocation)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LiveLocationMapViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFamilyUseCase: GetFamilyUseCase,
    private val getLiveLocationUseCase: GetLiveLocationUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val getAutocompletePredictionsUseCase: GetAutocompletePredictionsUseCase,
    private val locationRepository: LocationRepository,
    networkObserver: NetworkObserver
) : ViewModel() {

    companion object {
        private const val TAG = "LiveLocationMapViewModel"
    }

    private val _userLiveLocations = MutableStateFlow<List<UserLiveLocation>>(emptyList())
    val userLiveLocations: StateFlow<List<UserLiveLocation>> = _userLiveLocations.asStateFlow()

    private val _currentUserLocation = MutableStateFlow<UserLiveLocation?>(null)
    val currentUserLocation: StateFlow<UserLiveLocation?> = _currentUserLocation.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedUser = MutableStateFlow<UserLiveLocation?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _locationHistory = MutableStateFlow<List<TrackedLocation>>(emptyList())
    val locationHistory: StateFlow<List<TrackedLocation>> = _locationHistory.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private var locationHistoryJob: Job? = null

    val networkStatus: StateFlow<NetworkStatus> = networkObserver.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.Unknown)

    val autocompletePredictions: StateFlow<List<Place>> = searchText
        .debounce(300)
        .combine(networkStatus) { query, status ->
            query to status
        }
        .flatMapLatest { (query, status) ->
            if (query.isBlank() || status != NetworkStatus.Connected) {
                flowOf(emptyList())
            } else {
                getAutocompletePredictionsUseCase(query)
                    .catch { e ->
                        _errorMessage.value = when {
                            e.message?.contains("quota", ignoreCase = true) == true ->
                                "Search quota exceeded. Please try again later."
                            e.message?.contains("billing", ignoreCase = true) == true ->
                                "Places API billing not enabled."
                            else -> "Search temporarily unavailable."
                        }
                        emit(emptyList())
                    }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val userLiveLocationsFlow = getCurrentUserUseCase()
        .flowOn(Dispatchers.IO)
        .flatMapLatest { currentUser ->
            if (currentUser == null) return@flatMapLatest flowOf(emptyList())

            val friendsSharing = currentUser.sharingWithFriends
            val circlesSharing = currentUser.sharingWithFamily
            val uid = currentUser.uid

            // Get current user's live location
            val currentUserLocationFlow = getLiveLocationUseCase(listOf(uid))
                .flowOn(Dispatchers.IO)
                .map { locations: Map<String, LiveLocation> ->
                    locations[uid]?.let { location ->
                        UserLiveLocation(currentUser, location)
                    }
                }

            val familyFlow = if (circlesSharing.isNotEmpty()) {
                getFamilyUseCase().flowOn(Dispatchers.IO)
            } else {
                flowOf(emptyList())
            }

            familyFlow.flatMapLatest { families ->
                val familyMemberUids = families
                    .filter { it.id in circlesSharing }
                    .flatMap { it.members }

                val sharedIds = (friendsSharing + familyMemberUids).distinct().filter { it != uid && it.isNotBlank() }

                val friendsLocationsFlow = if (sharedIds.isNotEmpty()) {
                    val usersFlow = getUsersUseCase(sharedIds).flowOn(Dispatchers.IO)
                    val locationsFlow = getLiveLocationUseCase(sharedIds).flowOn(Dispatchers.IO)

                    usersFlow.combine(locationsFlow) { users: List<User>, locations: Map<String, LiveLocation> ->
                        users.mapNotNull { user ->
                            locations[user.uid]?.let { location ->
                                UserLiveLocation(user, location)
                            }
                        }
                    }
                } else {
                    flowOf(emptyList<UserLiveLocation>())
                }

                currentUserLocationFlow.combine(friendsLocationsFlow) { currentUserLoc: UserLiveLocation?, friendsLocs: List<UserLiveLocation> ->
                    val result = mutableListOf<UserLiveLocation>()
                    currentUserLoc?.let { result.add(it) }
                    result.addAll(friendsLocs)
                    result
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userLiveLocationsFlow.collect { combinedData ->
                    // Separate current user location from friends
                    val currentUserId = try {
                        getCurrentUserUseCase().first()?.uid
                    } catch (e: Exception) {
                        android.util.Log.w(TAG, "Failed to get current user ID: ${e.message}")
                        null
                    }

                    val currentUser = combinedData.find { it.user.uid == currentUserId }
                    val friends = combinedData.filter { it.user.uid != currentUserId }

                    _currentUserLocation.value = currentUser
                    _userLiveLocations.value = friends
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error in userLiveLocationsFlow collection: ${e.message}", e)
                _currentUserLocation.value = null
                _userLiveLocations.value = emptyList()
            }
        }
    }

    val searchedUsers = searchText
        .combine(userLiveLocations) { text, users ->
            try {
                if (text.isBlank()) {
                    emptyList()
                } else {
                    users.filter {
                        it.user.displayName.contains(text, ignoreCase = true) ||
                        it.user.email.contains(text, ignoreCase = true) ||
                        it.user.phone.contains(text, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Error filtering searched users: ${e.message}")
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchTextChanged(text: String) {
        // Sanitize input: remove newlines, limit length, filter invalid characters
        val sanitized = text
            .replace(Regex("[\\n\\r\\t]"), " ") // Replace newlines/tabs with space
            .take(200) // Limit to reasonable length
            .trim()
        _searchText.value = sanitized
    }

    fun clearSearchText() {
        _searchText.value = ""
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun addToSearchHistory(query: String) {
        if (query.isNotBlank()) {
            _searchHistory.update {
                (listOf(query) + it).distinct().take(5)
            }
        }
    }

    fun onUserSelected(user: UserLiveLocation) {
        addToSearchHistory(searchText.value)
        _selectedUser.value = user
        _selectedPlace.value = null
        clearSearchText()
    }

    fun onPlaceSelected(place: Place) {
        addToSearchHistory(place.name)
        _selectedPlace.value = place
        _selectedUser.value = null
        clearSearchText()
    }

    fun onHistorySelected(query: String) {
        _searchText.value = query
    }

    fun onMapClicked() {
        _selectedUser.value = null
        _selectedPlace.value = null
    }

    fun loadLocationHistoryForUser(userId: String, startDate: Date? = null, endDate: Date? = null) {
        locationHistoryJob?.cancel()
        _isLoadingHistory.value = true
        _locationHistory.value = emptyList()
        locationHistoryJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                locationRepository.getLocationHistoryForUser(userId, startDate, endDate)
                    .collect { locations ->
                        _locationHistory.value = locations
                        _isLoadingHistory.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load location history: ${e.message}"
                _isLoadingHistory.value = false
            }
        }
    }

    fun clearLocationHistory() {
        locationHistoryJob?.cancel()
        locationHistoryJob = null
        _locationHistory.value = emptyList()
        _isLoadingHistory.value = false
    }

    fun refreshData() {
        // This will trigger the flows to refresh since they are reactive
        // The data will automatically update through the existing flow subscriptions
    }
}
