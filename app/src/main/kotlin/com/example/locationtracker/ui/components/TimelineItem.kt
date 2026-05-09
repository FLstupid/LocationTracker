package com.example.locationtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.locationtracker.domain.model.Activity
import com.example.locationtracker.domain.enums.ActivityType

/**
 * Timeline item component for displaying an activity in the feed.
 * Shows user avatar, activity message, and relative time.
 */
@Composable
fun TimelineItem(
    activity: Activity,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = if (activity.isRead) {
            CardDefaults.cardElevation(defaultElevation = 1.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = 4.dp)
        },
        colors = if (activity.isRead) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            UserAvatar(
                photoUrl = activity.userPhotoUrl,
                displayName = activity.userName,
                size = 48.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Activity content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Activity message
                Text(
                    text = activity.getFormattedMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (activity.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.padding(2.dp))
                
                // Relative time
                Text(
                    text = activity.getRelativeTimeText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Activity type icon
            Text(
                text = activity.type.icon,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete activity",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Compact activity item for smaller displays or lists
 */
@Composable
fun CompactActivityItem(
    activity: Activity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity type icon
        Text(
            text = activity.type.icon,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.getFormattedMessage(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (activity.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = activity.getRelativeTimeText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Unread indicator
        if (!activity.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(start = 8.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.Blue,
                        radius = size.minDimension / 2
                    )
                }
            }
        }
    }
}

/**
 * Activity type filter chip
 */
@Composable
fun ActivityTypeChip(
    type: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = type.icon)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = type.displayName)
            }
        },
        modifier = modifier
    )
}
