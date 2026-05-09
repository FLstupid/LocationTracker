package com.example.locationtracker.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.locationtracker.feature.auth.AuthScreen
import com.example.locationtracker.feature.auth.SignUpScreen
import com.example.locationtracker.feature.family.FamilyDetailScreen
import com.example.locationtracker.feature.feed.ActivityFeedScreen
import com.example.locationtracker.feature.friends.FindFriendsScreen
import com.example.locationtracker.feature.friends.FriendRequestsScreen
import com.example.locationtracker.feature.live_location.LiveLocationMapScreen
import com.example.locationtracker.feature.location_history.LocationHistoryScreen
import com.example.locationtracker.feature.main.MainDestination
import com.example.locationtracker.feature.main.MainViewModel
import com.example.locationtracker.feature.onboarding.OnboardingScreen
import com.example.locationtracker.feature.sharing.SharingScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val destination by mainViewModel.destination.collectAsState()

    // Navigate when destination changes
    LaunchedEffect(destination) {
        when (destination) {
            MainDestination.Loading -> {
                // Stay on loading screen
            }
            MainDestination.Auth -> {
                navController.navigate("signIn") {
                    popUpTo("loading") { inclusive = true }
                }
            }
            MainDestination.Onboarding -> {
                navController.navigate("onboarding") {
                    popUpTo("loading") { inclusive = true }
                }
            }
            MainDestination.Home -> {
                navController.navigate("main") {
                    popUpTo("loading") { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        composable("signIn") {
            AuthScreen(navController)
        }
        composable("signUp") {
            SignUpScreen(navController)
        }
        composable("main") {
            BottomNavigationHost(navController)
        }
        composable(
            route = "familyDetail/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            FamilyDetailScreen(navController, familyId)
        }
        composable("find_friends") {
            FindFriendsScreen(navController)
        }
        composable("friend_requests") {
            FriendRequestsScreen(navController)
        }
        composable("live_location") {
            LiveLocationMapScreen(navController)
        }
        composable("location_history") {
            LocationHistoryScreen(navController)
        }
        composable("activity_feed") {
            ActivityFeedScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
    }
}
