package com.example.locationtracker.feature.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.usecase.GetFamilyDetailUseCaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyDetailViewModel @Inject constructor(
    private val getFamilyDetailUseCase: GetFamilyDetailUseCaseUseCase
) : ViewModel() {

    private val familyId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val family: StateFlow<Family?> =
        familyId
            .filterNotNull()
            .flatMapLatest { id ->
                getFamilyDetailUseCase(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    fun loadFamily(id: String) {
        if (familyId.value == id) return
        familyId.value = id
    }
}


