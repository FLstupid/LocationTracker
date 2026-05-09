package com.example.locationtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.locationtracker.core.base.BaseActivity
import com.example.locationtracker.core.navigation.AppNavHost
import com.example.locationtracker.core.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle notification taps
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Handle deep links from notifications
     */
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val notificationType = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
        val userId = intent.getStringExtra(NotificationHelper.EXTRA_USER_ID)
        val circleId = intent.getStringExtra(NotificationHelper.EXTRA_CIRCLE_ID)
        val deepLink = intent.getStringExtra(NotificationHelper.EXTRA_DEEP_LINK)

        Log.d(TAG, "Notification tap - Action: $action, Type: $notificationType, UserId: $userId")

        when (action) {
            "ACTION_ACCEPT_FRIEND_REQUEST" -> {
                // Navigate to friend requests screen
                Log.d(TAG, "Accept friend request from user: $userId")
                // The friend request will be handled by FriendRequestsScreen
                // For now, just navigate to the screen
            }
            "ACTION_DECLINE_FRIEND_REQUEST" -> {
                // Navigate to friend requests screen
                Log.d(TAG, "Decline friend request from user: $userId")
                // The friend request will be handled by FriendRequestsScreen
            }
            else -> {
                // Handle generic notification tap based on type
                deepLink?.let { link ->
                    Log.d(TAG, "Navigate to deep link: $link")
                    // Parse deep link and navigate accordingly
                    when {
                        link.contains("friend_requests") -> {
                            // Navigation will be handled by NavHost
                        }
                        link.contains("activity_feed") -> {
                            // Navigation will be handled by NavHost
                        }
                        link.contains("family/") -> {
                            circleId?.let {
                                // Navigation will be handled by NavHost
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Screen() {
        var showPermissionDeniedDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        val backgroundLocationLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Log.w(TAG, "Background location not granted. Tracking will be foreground-limited.")
            }
        }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val hasForegroundPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasForegroundPermission) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                // Handle permission denied
                showPermissionDeniedDialog = true
            }
        }

        if (showPermissionDeniedDialog) {
            PermissionDeniedDialog(
                onDismiss = {
                    showPermissionDeniedDialog = false
                },
                onGoToSettings = {
                    showPermissionDeniedDialog = false

                    // Navigate to app settings
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = android.net.Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }
            )
        }

        LaunchedEffect(Unit) {
            launcher.launch(permissionsToRequest)
        }
        AppNavHost()
    }
}

@Composable
fun PermissionDeniedDialog(onDismiss: () -> Unit, onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Denied") },
        text = { Text("Location permissions are required to use this feature.") },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
