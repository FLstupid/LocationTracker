package com.example.locationtracker.feature.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.usecase.CreateFamilyUseCase
import com.example.locationtracker.domain.usecase.GetFamilyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyUiState(
    val family: List<Family> = emptyList(),
    val createFamilyStatus: String? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null
)

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val getFamilyUseCase: GetFamilyUseCase,
    private val createFamilyUseCase: CreateFamilyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        loadFamilies()
    }

    private fun loadFamilies() {
        viewModelScope.launch(Dispatchers.IO) {
            getFamilyUseCase()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = UiError.from(e),
                            isLoading = false
                        )
                    }
                }
                .collect { families ->
                    _uiState.update { it.copy(family = families, isLoading = false) }
                }
        }
    }

    fun createFamily(name: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = createFamilyUseCase(name)
                _uiState.update {
                    if (result) {
                        it.copy(
                            createFamilyStatus = "Family created successfully!",
                            isLoading = false
                        )
                    } else {
                        it.copy(
                            createFamilyStatus = "Failed to create circle.",
                            isLoading = false
                        )
                    }
                }
                // Refresh will happen automatically via the flow in init
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        createFamilyStatus = "Error creating circle: ${e.message}",
                        error = UiError.from(e),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearCreateFamilyStatus() {
        _uiState.update { it.copy(createFamilyStatus = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
