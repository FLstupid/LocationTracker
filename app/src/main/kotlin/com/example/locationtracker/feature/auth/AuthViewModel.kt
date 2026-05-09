package com.example.locationtracker.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.core.base.UiError
import com.example.locationtracker.domain.usecase.SignInUseCase
import com.example.locationtracker.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val authError: String? = null,
    val isAuthenticated: Boolean = false,
    val error: UiError? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(authError = null, error = null) }
    }

    fun signUp(name: String, email: String, phone: String, password: String) {
        _uiState.update { it.copy(isLoading = true, authError = null, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = signUpUseCase(name, email, password, phone)
                _uiState.update {
                    if (result) {
                        it.copy(isLoading = false, isAuthenticated = true)
                    } else {
                        it.copy(isLoading = false, authError = "Sign up failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = e.message,
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, authError = null, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = signInUseCase(email, password)
                _uiState.update {
                    if (result) {
                        it.copy(isLoading = false, isAuthenticated = true)
                    } else {
                        it.copy(isLoading = false, authError = "Sign in failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = e.message,
                        error = UiError.from(e)
                    )
                }
            }
        }
    }

    fun signInWithGoogle() {
        // Future: Implement Google Sign-In
        // Requires Google Sign-In SDK and Firebase Auth integration
        _uiState.update { it.copy(
            authError = "Google Sign-In coming soon!"
        ) }
    }

    fun signInWithFacebook() {
        // Future: Implement Facebook Sign-In
        // Requires Facebook SDK and Firebase Auth integration
        _uiState.update { it.copy(
            authError = "Facebook Sign-In coming soon!"
        ) }
    }

}
