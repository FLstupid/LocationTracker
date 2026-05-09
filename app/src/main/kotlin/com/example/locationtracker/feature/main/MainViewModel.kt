package com.example.locationtracker.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.domain.repository.SettingsRepository
import com.example.locationtracker.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

sealed class MainDestination {
    data object Loading : MainDestination()
    data object Auth : MainDestination()
    data object Onboarding : MainDestination()
    data object Home : MainDestination()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _destination = MutableStateFlow<MainDestination>(MainDestination.Loading)
    val destination = _destination.asStateFlow()

    val settings = settingsRepository.getSettings()

    init {
        determineStartDestination()
        observeAuthState()
    }

    // Listen to Firebase auth state changes
    private fun observeAuthState() {
        viewModelScope.launch {
            callbackFlow {
                val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                    trySend(auth.currentUser != null)
                }
                firebaseAuth.addAuthStateListener(authStateListener)
                awaitClose {
                    firebaseAuth.removeAuthStateListener(authStateListener)
                }
            }.collect { isAuthenticated ->
                // If user logs out while in the app, redirect to auth
                if (!isAuthenticated && _destination.value != MainDestination.Auth && _destination.value != MainDestination.Loading) {
                    Log.d(TAG, "User logged out, redirecting to auth")
                    _destination.value = MainDestination.Auth
                }
            }
        }
    }

    private fun determineStartDestination() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                Log.d(TAG, "Determining start destination...")
                val firebaseUser = firebaseAuth.currentUser
                Log.d(TAG, "Firebase user: ${firebaseUser?.uid}")

                if (firebaseUser == null) {
                    Log.d(TAG, "No Firebase user - navigating to Auth")
                    _destination.value = MainDestination.Auth
                    return@launch
                }

                // Verify user actually exists in our database
                Log.d(TAG, "Checking if user exists in database...")
                val user = try {
                    withTimeout(5000) { // 5 second timeout
                        userRepository.getCurrentUser().first()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user from database", e)
                    null
                }

                if (user == null) {
                    Log.d(TAG, "User not found in database - signing out and navigating to Auth")
                    firebaseAuth.signOut()
                    _destination.value = MainDestination.Auth
                    return@launch
                }

                Log.d(TAG, "User found: ${user.displayName}")
                Log.d(TAG, "Checking onboarding status...")
                val isOnboarded = settingsRepository.isOnboardingCompleted()
                Log.d(TAG, "Is onboarded: $isOnboarded")

                _destination.value = if (isOnboarded) {
                    Log.d(TAG, "Navigating to Home")
                    MainDestination.Home
                } else {
                    Log.d(TAG, "Navigating to Onboarding")
                    MainDestination.Onboarding
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error determining destination", e)
                // Default to auth on error
                _destination.value = MainDestination.Auth
            }
        }
    }
}
