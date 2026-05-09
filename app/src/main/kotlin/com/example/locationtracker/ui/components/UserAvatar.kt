package com.example.locationtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.locationtracker.domain.enums.PresenceStatus
import com.example.locationtracker.domain.model.User

/**
 * Displays user avatar with photo or initials placeholder
 * Includes optional presence indicator overlay
 */
@Composable
fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showPresence: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Avatar image or placeholder
        if (user.photoUrl != null && user.photoUrl.isNotBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "${user.displayName}'s avatar",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = null,
                error = null
            )
        } else {
            AvatarPlaceholder(
                displayName = user.displayName,
                size = size
            )
        }

        // Presence indicator in bottom-right corner
        if (showPresence) {
            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                PresenceIndicator(
                    status = user.presenceStatus,
                    size = (size.value * 0.25f).dp,
                    showBorder = true
                )
            }
        }
    }
}

/**
 * Simple circular avatar from URL
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    if (photoUrl != null && photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "$displayName's avatar",
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        AvatarPlaceholder(
            displayName = displayName,
            size = size,
            modifier = modifier
        )
    }
}

/**
 * Placeholder avatar with initials
 */
@Composable
fun AvatarPlaceholder(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (displayName.isNotBlank()) {
            // Show initials
            val initials = getInitials(displayName)
            androidx.compose.material3.Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        } else {
            // Show default person icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier.size(size * 0.6f),
                tint = contentColor
            )
        }
    }
}

/**
 * Get initials from display name (first letter of first two words)
 */
private fun getInitials(displayName: String): String {
    val words = displayName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(1).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}
