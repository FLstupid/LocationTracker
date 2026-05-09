package com.example.locationtracker.feature.friends

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.ui.components.ConfirmationDialog
import com.example.locationtracker.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(navController: NavController, viewModel: FriendsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var requestToReject by remember { mutableStateOf<FriendRequest?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Show toast for status messages
    androidx.compose.runtime.LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.friendRequests.isEmpty()) {
            EmptyState(
                icon = Icons.Default.PersonAdd,
                title = "No friend requests",
                message = "When someone sends you a friend request, it will appear here",
                actionText = "Find Friends",
                onActionClick = { navController.navigate("find_friends") }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                
                items(uiState.friendRequests) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = request.fromName.ifBlank { request.fromPhone },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (request.fromName.isNotBlank()) {
                                    Text(
                                        text = request.fromPhone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "wants to be your friend",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.acceptFriendRequest(request) },
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Accept")
                                }
                                OutlinedButton(
                                    onClick = { requestToReject = request },
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Decline")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for rejecting friend request
    requestToReject?.let { request ->
        ConfirmationDialog(
            title = "Decline Friend Request?",
            message = "Are you sure you want to decline the friend request from ${request.fromPhone}?",
            confirmText = "Decline",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.rejectFriendRequest(request)
                requestToReject = null
            },
            onDismiss = { requestToReject = null },
            isDestructive = true
        )
    }
}
