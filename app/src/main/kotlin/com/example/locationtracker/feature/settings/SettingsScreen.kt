package com.example.locationtracker.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

import com.example.locationtracker.domain.model.AppTheme
import com.example.locationtracker.domain.model.HistoryRetention
import com.example.locationtracker.domain.model.MapStyle
import com.example.locationtracker.domain.model.MeasurementUnits
import com.example.locationtracker.domain.model.UpdateFrequency
import com.example.locationtracker.ui.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUpdateFrequencyDialog by remember { mutableStateOf(false) }
    var showHistoryRetentionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMapStyleDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Location Settings
            item {
                SettingsSectionHeader(title = "Location Settings", icon = Icons.Default.LocationOn)
            }
            item {
                SettingsItem(
                    title = "Update Frequency",
                    subtitle = settings.locationUpdateFrequency.name.replace("_", " "),
                    onClick = { showUpdateFrequencyDialog = true }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Notification Settings
            item {
                SettingsSectionHeader(title = "Notifications", icon = Icons.Default.Notifications)
            }
            item {
                SettingsSwitchItem(
                    title = "Enable Notifications",
                    subtitle = "Receive all app notifications",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = settingsViewModel::updateNotificationsEnabled
                )
            }
            if (settings.notificationsEnabled) {
                item {
                    SettingsSwitchItem(
                        title = "Friend Requests",
                        subtitle = "Get notified of new friend requests",
                        checked = settings.friendRequestNotifications,
                        onCheckedChange = settingsViewModel::updateFriendRequestNotifications
                    )
                }
                item {
                    SettingsSwitchItem(
                        title = "Location Alerts",
                        subtitle = "Friend location changes",
                        checked = settings.locationAlertNotifications,
                        onCheckedChange = settingsViewModel::updateLocationAlertNotifications
                    )
                }
                item {
                    SettingsSwitchItem(
                        title = "Activity Updates",
                        subtitle = "Friend activity notifications",
                        checked = settings.activityUpdateNotifications,
                        onCheckedChange = settingsViewModel::updateActivityUpdateNotifications
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Privacy Settings
            item {
                SettingsSectionHeader(title = "Privacy", icon = Icons.Default.Shield)
            }
            item {
                SettingsSwitchItem(
                    title = "Share Location",
                    subtitle = "Allow friends to see your location",
                    checked = settings.shareLocationEnabled,
                    onCheckedChange = settingsViewModel::updateShareLocationEnabled
                )
            }
            item {
                SettingsItem(
                    title = "Location History Retention",
                    subtitle = when (settings.locationHistoryRetention) {
                        HistoryRetention.FOREVER -> "Forever"
                        else -> "${settings.locationHistoryRetention.days} days"
                    },
                    onClick = { showHistoryRetentionDialog = true }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Share Battery Level",
                    subtitle = "Show battery status to friends",
                    checked = settings.shareBatteryLevel,
                    onCheckedChange = settingsViewModel::updateShareBatteryLevel
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // App Settings
            item {
                SettingsSectionHeader(title = "Appearance", icon = Icons.Default.DarkMode)
            }
            item {
                SettingsItem(
                    title = "Theme",
                    subtitle = settings.theme.name.replace("_", " "),
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsItem(
                    title = "Map Style",
                    subtitle = settings.mapStyle.name,
                    icon = Icons.Default.Map,
                    onClick = { showMapStyleDialog = true }
                )
            }
            item {
                SettingsItem(
                    title = "Units",
                    subtitle = settings.units.name,
                    icon = Icons.Default.Straighten,
                    onClick = { showUnitsDialog = true }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Account Settings
            item {
                SettingsSectionHeader(title = "Account", icon = Icons.Default.Person)
            }
            item {
                SettingsItem(
                    title = "Profile",
                    subtitle = "View and edit your profile",
                    onClick = { navController.navigate("profile") }
                )
            }
            item {
                SettingsItem(
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = { showLogoutDialog = true }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // About
            item {
                SettingsSectionHeader(title = "About", icon = Icons.Default.Info)
            }
            item {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
                        context.startActivity(intent)
                    }
                )
            }
            item {
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/terms"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // Dialogs
    if (showUpdateFrequencyDialog) {
        SelectionDialog(
            title = "Update Frequency",
            options = UpdateFrequency.entries.toList(),
            selectedOption = settings.locationUpdateFrequency,
            onOptionSelected = {
                settingsViewModel.updateLocationUpdateFrequency(it)
                showUpdateFrequencyDialog = false
            },
            onDismiss = { showUpdateFrequencyDialog = false },
            optionLabel = { it.name.replace("_", " ") }
        )
    }

    if (showHistoryRetentionDialog) {
        SelectionDialog(
            title = "Location History Retention",
            options = HistoryRetention.entries.toList(),
            selectedOption = settings.locationHistoryRetention,
            onOptionSelected = {
                settingsViewModel.updateLocationHistoryRetention(it)
                showHistoryRetentionDialog = false
            },
            onDismiss = { showHistoryRetentionDialog = false },
            optionLabel = { if (it == HistoryRetention.FOREVER) "Forever" else "${it.days} days" }
        )
    }

    if (showThemeDialog) {
        SelectionDialog(
            title = "Theme",
            options = AppTheme.entries.toList(),
            selectedOption = settings.theme,
            onOptionSelected = {
                settingsViewModel.updateTheme(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
            optionLabel = { it.name }
        )
    }

    if (showMapStyleDialog) {
        SelectionDialog(
            title = "Map Style",
            options = MapStyle.entries.toList(),
            selectedOption = settings.mapStyle,
            onOptionSelected = {
                settingsViewModel.updateMapStyle(it)
                showMapStyleDialog = false
            },
            onDismiss = { showMapStyleDialog = false },
            optionLabel = { it.name }
        )
    }

    if (showUnitsDialog) {
        SelectionDialog(
            title = "Measurement Units",
            options = MeasurementUnits.entries.toList(),
            selectedOption = settings.units,
            onOptionSelected = {
                settingsViewModel.updateUnits(it)
                showUnitsDialog = false
            },
            onDismiss = { showUnitsDialog = false },
            optionLabel = { it.name }
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout",
            message = "Are you sure you want to logout?",
            confirmText = "Logout",
            dismissText = "Cancel",
            onConfirm = {
                navController.navigate("signIn") {
                    popUpTo("home") { inclusive = true }
                }
            },
            onDismiss = { showLogoutDialog = false },
            isDestructive = false
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun <T> SelectionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    optionLabel: (T) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Text(
                            text = optionLabel(option),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
