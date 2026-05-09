package com.example.locationtracker.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.domain.model.Activity
import com.example.locationtracker.domain.enums.ActivityType
import com.example.locationtracker.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityFeedUiState(
    val activities: List<Activity> = emptyList(),
    val filteredTypes: List<ActivityType> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Int = 0,
    val hasMore: Boolean = true
)

@HiltViewModel
class ActivityFeedViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ActivityFeedUiState())
    val uiState: StateFlow<ActivityFeedUiState> = _uiState.asStateFlow()
    
    private var lastActivityId: String? = null
    private val pageSize = 20
    
    init {
        loadActivities()
        observeUnreadCount()
    }
    
    /**
     * Load initial activities
     */
    fun loadActivities() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            activityRepository.getActivityFeed(limit = pageSize)
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load activities: ${error.message}"
                        )
                    }
                }
                .collect { activities ->
                    _uiState.update { 
                        it.copy(
                            activities = activities,
                            isLoading = false,
                            hasMore = activities.size >= pageSize
                        )
                    }
                    
                    // Store last activity ID for pagination
                    lastActivityId = activities.lastOrNull()?.id
                }
        }
    }
    
    /**
     * Load more activities (pagination)
     */
    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            activityRepository.getActivityFeed(limit = pageSize, lastActivityId = lastActivityId)
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load more: ${error.message}"
                        )
                    }
                }
                .collect { newActivities ->
                    val currentActivities = _uiState.value.activities
                    val combinedActivities = (currentActivities + newActivities).distinctBy { it.id }
                    
                    _uiState.update { 
                        it.copy(
                            activities = combinedActivities,
                            isLoading = false,
                            hasMore = newActivities.size >= pageSize
                        )
                    }
                    
                    // Update last activity ID
                    lastActivityId = newActivities.lastOrNull()?.id
                }
        }
    }
    
    /**
     * Refresh activities (pull-to-refresh)
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        lastActivityId = null
        
        viewModelScope.launch {
            activityRepository.getActivityFeed(limit = pageSize)
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            errorMessage = "Failed to refresh: ${error.message}"
                        )
                    }
                }
                .collect { activities ->
                    _uiState.update { 
                        it.copy(
                            activities = activities,
                            isRefreshing = false,
                            hasMore = activities.size >= pageSize
                        )
                    }
                    
                    lastActivityId = activities.lastOrNull()?.id
                }
        }
    }
    
    /**
     * Filter activities by type
     */
    fun filterByType(types: List<ActivityType>) {
        _uiState.update { it.copy(filteredTypes = types, isLoading = true) }
        
        if (types.isEmpty()) {
            // No filter, load all activities
            loadActivities()
            return
        }
        
        viewModelScope.launch {
            activityRepository.getActivitiesByType(types, limit = pageSize)
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to filter: ${error.message}"
                        )
                    }
                }
                .collect { activities ->
                    _uiState.update { 
                        it.copy(
                            activities = activities,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    /**
     * Clear filter
     */
    fun clearFilter() {
        _uiState.update { it.copy(filteredTypes = emptyList()) }
        loadActivities()
    }
    
    /**
     * Mark activity as read
     */
    fun markAsRead(activityId: String) {
        viewModelScope.launch {
            activityRepository.markAsRead(activityId)
        }
    }
    
    /**
     * Mark all activities as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            activityRepository.markAllAsRead()
        }
    }
    
    /**
     * Delete activity
     */
    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
            
            // Update UI
            _uiState.update { state ->
                state.copy(
                    activities = state.activities.filter { it.id != activityId }
                )
            }
        }
    }
    
    /**
     * Clear all activities
     */
    fun clearAll() {
        viewModelScope.launch {
            activityRepository.clearAllActivities()
            _uiState.update { it.copy(activities = emptyList()) }
        }
    }
    
    /**
     * Observe unread count
     */
    private fun observeUnreadCount() {
        viewModelScope.launch {
            activityRepository.getUnreadCount()
                .catch { error ->
                    // Ignore errors for unread count
                }
                .collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
