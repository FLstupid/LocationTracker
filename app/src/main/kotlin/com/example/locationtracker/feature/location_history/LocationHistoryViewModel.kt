package com.example.locationtracker.feature.location_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.model.TrackedLocation
import com.example.locationtracker.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class LocationHistoryUiState(
    val trackedLocations: List<TrackedLocation> = emptyList(),
    val selectedDate: Date? = null,
    val searchStartDate: Date? = null,
    val searchEndDate: Date? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null
)

@HiltViewModel
class LocationHistoryViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationHistoryUiState())
    val uiState: StateFlow<LocationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadLocationHistory(null, null)
    }

    private fun loadLocationHistory(startDate: Date?, endDate: Date?) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            locationRepository.getLocationHistory(startDate, endDate)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = UiError.from(e),
                            isLoading = false
                        )
                    }
                }
                .collect { locations ->
                    _uiState.update {
                        it.copy(
                            trackedLocations = locations,
                            selectedDate = if (it.selectedDate == null && locations.isNotEmpty()) {
                                locations.last().timestamp
                            } else {
                                it.selectedDate
                            },
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setSelectedDate(date: Date?) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setSearchDateRange(startDate: Date?, endDate: Date?) {
        _uiState.update {
            it.copy(
                searchStartDate = startDate,
                searchEndDate = endDate,
                selectedDate = null  // Clear daily selection when searching by range
            )
        }
        loadLocationHistory(startDate, endDate)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}