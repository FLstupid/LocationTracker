package com.example.locationtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.locationtracker.domain.enums.PresenceStatus

/**
 * Small circular indicator showing user's online status
 * Can be overlaid on profile photos or shown standalone
 */
@Composable
fun PresenceIndicator(
    status: PresenceStatus,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    showBorder: Boolean = true
) {
    val color = when (status) {
        PresenceStatus.ONLINE -> Color(0xFF4CAF50) // Green
        PresenceStatus.AWAY -> Color(0xFFFFC107) // Amber/Yellow
        PresenceStatus.OFFLINE -> Color(0xFF9E9E9E) // Gray
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
    )
}
