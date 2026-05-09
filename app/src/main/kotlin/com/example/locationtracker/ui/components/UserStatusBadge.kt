package com.example.locationtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.locationtracker.domain.enums.PresenceStatus
import com.example.locationtracker.domain.model.UserPresence

/**
 * Comprehensive user status badge showing presence, battery, and location sharing
 * Used in friend lists and detail views
 */
@Composable
fun UserStatusBadge(
    presence: UserPresence,
    modifier: Modifier = Modifier,
    showBattery: Boolean = true,
    showLocation: Boolean = true,
    showLastSeen: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Presence status with last seen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresenceIndicator(
                status = presence.status,
                size = 10.dp
            )

            val statusText = when (presence.status) {
                PresenceStatus.ONLINE -> "Active now"
                PresenceStatus.AWAY -> if (showLastSeen) presence.getLastSeenText() else "Away"
                PresenceStatus.OFFLINE -> if (showLastSeen) presence.getLastSeenText() else "Offline"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Battery level (if low)
        if (showBattery && presence.isBatteryLow() && presence.batteryLevel != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = "Low battery",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFF44336) // Red
                )
                Text(
                    text = "${presence.batteryLevel}% battery",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Location sharing status
        if (showLocation && presence.isSharing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Sharing location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = presence.currentPlace ?: "Sharing location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact version showing just the status text with indicator
 */
@Composable
fun CompactUserStatus(
    presence: UserPresence,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PresenceIndicator(
            status = presence.status,
            size = 8.dp
        )

        val statusText = when (presence.status) {
            PresenceStatus.ONLINE -> "Active"
            PresenceStatus.AWAY -> "Away"
            PresenceStatus.OFFLINE -> "Offline"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Show battery alert if low
        if (presence.isBatteryLow()) {
            Icon(
                imageVector = Icons.Default.BatteryAlert,
                contentDescription = "Low battery",
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFF44336)
            )
        }
    }
}
