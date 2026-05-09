package com.example.locationtracker.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.locationtracker.feature.family.FamilyScreen
import com.example.locationtracker.feature.feed.ActivityFeedScreen
import com.example.locationtracker.feature.friends.FriendsScreen
import com.example.locationtracker.feature.home.HomeScreen
import com.example.locationtracker.feature.profile.ProfileScreen
import com.example.locationtracker.feature.settings.SettingsScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationHost(navController: NavHostController) {
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Family", Icons.Default.People, "family"),
        BottomNavItem("Friends", Icons.Default.Person, "friends"),
        BottomNavItem("Profile", Icons.Default.AccountCircle, "profile"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )

    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var showActivityFeed by rememberSaveable { mutableStateOf(false) }

    // Sync pager with selected item
    LaunchedEffect(selectedItem) {
        pagerState.animateScrollToPage(selectedItem)
    }

    // Sync selected item with pager
    LaunchedEffect(pagerState.currentPage) {
        selectedItem = pagerState.currentPage
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = true, // Always show bottom bar for pager
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedItem == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showActivityFeed) {
                ActivityFeedScreen(
                    onNavigateBack = { showActivityFeed = false }
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(navController, onNavigateToActivityFeed = { showActivityFeed = true })
                        1 -> FamilyScreen(navController)
                        2 -> FriendsScreen(navController)
                        3 -> ProfileScreen(navController)
                        4 -> SettingsScreen(navController)
                    }
                }
            }
        }
    }
}