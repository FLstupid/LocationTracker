package com.example.locationtracker.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents an onboarding page with its content and metadata
 */
sealed class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
) {
    data object Welcome : OnboardingPage(
        title = "Welcome to LocationTracker",
        description = "Stay connected with your friends and family. Share your location in real-time and create family circles.",
        icon = Icons.Default.LocationOn,
        route = "welcome"
    )

    data object Permissions : OnboardingPage(
        title = "Location Permissions",
        description = "We need location permissions to share your location with friends and show their locations to you. Your privacy is our priority.",
        icon = Icons.Default.MyLocation,
        route = "permissions"
    )

    data object Notifications : OnboardingPage(
        title = "Stay Notified",
        description = "Get notified when friends arrive at important places, join your circles, or share their location with you.",
        icon = Icons.Default.Notifications,
        route = "notifications"
    )

    data object Profile : OnboardingPage(
        title = "Setup Your Profile",
        description = "Add a profile photo and display name so your friends can easily recognize you.",
        icon = Icons.Default.Person,
        route = "profile"
    )

    companion object {
        val pages = listOf(Welcome, Permissions, Notifications, Profile)
    }
}
