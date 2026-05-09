package com.example.locationtracker.feature.live_location

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locationtracker.core.utils.NetworkStatus
import com.example.locationtracker.domain.model.LatLng
import com.example.locationtracker.domain.model.Place
import com.example.locationtracker.feature.service.LocationService

enum class SearchMode { FRIENDS, LOCATION }

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationMapScreen(
    navController: NavController,
    viewModel: LiveLocationMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // --- UI State ---
    var searchMode by remember { mutableStateOf(SearchMode.FRIENDS) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showLocationHistory by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- ViewModel State ---
    val userLiveLocations by viewModel.userLiveLocations.collectAsState()
    val currentUserLocation by viewModel.currentUserLocation.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchedUsers by viewModel.searchedUsers.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val locationHistory by viewModel.locationHistory.collectAsState()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsState()
    val autocompletePredictions by viewModel.autocompletePredictions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Debug logging
    LaunchedEffect(userLiveLocations) {
        android.util.Log.d("LiveLocationMap", "User live locations updated: ${userLiveLocations.size} friends")
        userLiveLocations.forEach { location ->
            android.util.Log.d("LiveLocationMap", "Friend: ${location.user.displayName} at ${location.location.latitude}, ${location.location.longitude}")
        }
    }

    val showBottomSheet = selectedUser != null || selectedPlace != null

    // Show error message as Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // --- Service Lifecycle ---
    DisposableEffect(Unit) {
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live Location")
                        val totalLocations = userLiveLocations.size + (currentUserLocation?.let { 1 } ?: 0)
                        if (totalLocations > 0) {
                            Text(
                                text = "${totalLocations} location${if (totalLocations != 1) "s" else ""} on map",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val webViewRef = remember { mutableStateOf<WebView?>(null) }
            val lastMapData = remember { mutableStateOf<Triple<List<UserLiveLocation>, UserLiveLocation?, Place?>?>(null) }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                android.util.Log.d("LiveLocationMap", "Map page loaded successfully")
                            }

                            override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                android.util.Log.e("LiveLocationMap", "WebView error: ${error?.description}")
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                        webViewRef.value = this
                    }
                },
                update = { webView ->
                    // Only update if the map data has actually changed
                    val currentData = Triple(userLiveLocations, currentUserLocation, selectedPlace)
                    if (lastMapData.value != currentData) {
                        lastMapData.value = currentData
                        try {
                            webView.loadDataWithBaseURL(null, generateMapHtml(userLiveLocations, currentUserLocation, selectedPlace), "text/html", "UTF-8", null)
                        } catch (e: Exception) {
                            android.util.Log.e("LiveLocationMap", "Error updating map: ${e.message}", e)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Search UI
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchText,
                            onQueryChange = viewModel::onSearchTextChanged,
                            onSearch = {
                                focusManager.clearFocus()
                                isSearchActive = false
                            },
                            expanded = isSearchActive,
                            onExpandedChange = { isSearchActive = it },
                            placeholder = { Text("Search for friends or locations") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearSearchText() }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            }
                        )
                    },
                    expanded = isSearchActive,
                    onExpandedChange = { isSearchActive = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(selected = searchMode == SearchMode.FRIENDS, onClick = { searchMode = SearchMode.FRIENDS }, shape = MaterialTheme.shapes.medium, label = { Text("Friends") })
                            SegmentedButton(selected = searchMode == SearchMode.LOCATION, onClick = { searchMode = SearchMode.LOCATION }, shape = MaterialTheme.shapes.medium, label = { Text("Location") })
                        }
                        Spacer(Modifier.height(12.dp))

                        LazyColumn {
                            if (searchText.isBlank()) {
                                if (searchHistory.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Start typing to search for friends or locations",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(searchHistory, key = { it }) {
                                        ListItem(
                                            headlineContent = { Text(it) },
                                            leadingContent = { Icon(Icons.Default.History, null) },
                                            modifier = Modifier.clickable { viewModel.onHistorySelected(it) }
                                        )
                                    }
                                }
                            } else if (searchMode == SearchMode.FRIENDS) {
                                if (searchedUsers.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No friends found matching '$searchText'",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(searchedUsers, key = { it.user.uid }) {
                                        ListItem(
                                            headlineContent = { Text(it.user.displayName) },
                                            supportingContent = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(it.user.phone)
                                                    Spacer(Modifier.width(8.dp))
                                                    // You could add logic here to show if they're a friend or family member
                                                    // For now, just show the phone number
                                                }
                                            },
                                            modifier = Modifier.clickable {
                                                viewModel.onUserSelected(it)
                                                isSearchActive = false
                                            }
                                        )
                                    }
                                }
                            } else { // Location search
                                if (autocompletePredictions.isEmpty() && networkStatus != NetworkStatus.Connected) {
                                    item {
                                        Text(
                                            text = "Location search requires internet connection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else if (autocompletePredictions.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No locations found for '$searchText'",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(autocompletePredictions, key = { it.id }) {
                                        ListItem(
                                            headlineContent = { Text(it.name) },
                                            supportingContent = { Text(it.address) },
                                            leadingContent = { Icon(Icons.Default.LocationOn, null) },
                                            modifier = Modifier.clickable {
                                                viewModel.onPlaceSelected(it)
                                                isSearchActive = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Location count indicator
            val totalLocations = userLiveLocations.size + (currentUserLocation?.let { 1 } ?: 0)
            if (totalLocations > 0) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(totalLocations.toString())
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { /* Could show location list */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Locations on map")
                    }
                }
            }

            // Loading indicator when data is being fetched
            if (userLiveLocations.isEmpty() && networkStatus == NetworkStatus.Connected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading locations...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Make sure location services are enabled and your friends/family are sharing their location with you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = networkStatus == NetworkStatus.Disconnected,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You are currently offline. Some features may not be available.",
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }

            // Bottom Sheet for Details
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onMapClicked() },
                    sheetState = sheetState
                ) {
                    val title: String
                    val subtitle: String
                    val position: LatLng?

                    val currentSelectedUser = selectedUser
                    val currentSelectedPlace = selectedPlace

                    if (currentSelectedUser != null) {
                        title = currentSelectedUser.user.displayName
                        subtitle = currentSelectedUser.user.email
                        position = LatLng(currentSelectedUser.location.latitude, currentSelectedUser.location.longitude)
                    } else if (currentSelectedPlace != null) {
                        title = currentSelectedPlace.name
                        subtitle = currentSelectedPlace.address
                        position = currentSelectedPlace.latLng
                    } else {
                        // Fallback if both are null (shouldn't happen due to showBottomSheet logic)
                        title = "Unknown"
                        subtitle = "No location data"
                        position = null
                    }

                    Column(Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))

                        // Show location history button for users (not places)
                        if (selectedUser != null) {
                            Button(
                                onClick = {
                                    selectedUser?.let { user ->
                                        viewModel.loadLocationHistoryForUser(user.user.uid)
                                        showLocationHistory = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("View Location History")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                position?.let {
                                    try {
                                        val gmmIntentUri = "google.navigation:q=${it.latitude},${it.longitude}".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(context, "Google Maps app not installed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Get Directions")
                        }
                    }
                }
            }

            // Bottom Sheet for Location History
            if (showLocationHistory && selectedUser != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showLocationHistory = false
                        viewModel.clearLocationHistory()
                    },
                    sheetState = sheetState
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = "${selectedUser?.user?.displayName ?: "User"}'s Location History",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(16.dp))

                        if (isLoadingHistory) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (locationHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "No location history available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "This user may not have shared their location history",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(locationHistory, key = { it.id }) { location ->
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                location.timestamp?.let { timestamp ->
                                                    Text(
                                                        text = android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", timestamp).toString(),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val gmmIntentUri = "google.navigation:q=${location.latitude},${location.longitude}".toUri()
                                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                        mapIntent.setPackage("com.google.android.apps.maps")
                                                        context.startActivity(mapIntent)
                                                    } catch (_: ActivityNotFoundException) {
                                                        Toast.makeText(context, "Google Maps app not installed.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = "Navigate to this location"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateMapHtml(userLiveLocations: List<UserLiveLocation>, currentUserLocation: UserLiveLocation?, selectedPlace: Place?): String {
    // Calculate map center and zoom based on available data
    val allLocations = userLiveLocations + listOfNotNull(currentUserLocation)
    val (centerLat, centerLng, zoom) = when {
        allLocations.isNotEmpty() -> {
            // Center on all available locations
            val avgLat = allLocations.map { it.location.latitude }.average()
            val avgLng = allLocations.map { it.location.longitude }.average()
            Pair(avgLat, avgLng) to 12
        }
        selectedPlace != null -> {
            // Center on selected place
            Pair(selectedPlace.latLng.latitude, selectedPlace.latLng.longitude) to 15
        }
        else -> {
            // Default world view
            Pair(0.0, 0.0) to 2
        }
    }.let { (center, zoomLevel) -> Triple(center.first, center.second, zoomLevel) }

    val currentUserMarker = currentUserLocation?.let {
        val safeName = escapeJsString("You")
        """
        L.marker([${it.location.latitude}, ${it.location.longitude}])
            .addTo(map)
            .bindPopup('<b>$safeName</b><br/>Your current location<br/><small>Last updated: recently</small>');
        """
    } ?: ""

    val friendsMarkers = userLiveLocations.joinToString("") { userLocation ->
        val safeDisplayName = escapeJsString(userLocation.user.displayName)
        """
        L.marker([${userLocation.location.latitude}, ${userLocation.location.longitude}])
            .addTo(map)
            .bindPopup('<b>$safeDisplayName</b><br/>Last updated: recently');
        """
    }

    val placeMarker = selectedPlace?.let {
        val safePlaceName = escapeJsString(it.name)
        val safeAddress = escapeJsString(it.address)
        """
        L.marker([${it.latLng.latitude}, ${it.latLng.longitude}])
            .addTo(map)
            .bindPopup('<b>$safePlaceName</b><br/>$safeAddress');
        """
    } ?: ""

    val noDataMessage = if (userLiveLocations.isEmpty() && currentUserLocation == null && selectedPlace == null) {
        """
        L.marker([0, 0])
            .addTo(map)
            .bindPopup('No locations to show.<br/>Enable location services and ask your friends/family to share their location with you.');
        """
    } else ""

    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no">
        <meta name="description" content="Live Location Sharing Map">
        <title>LocationTracker - Live Map</title>
        <!-- Leaflet CSS -->
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
              integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
              crossorigin=""/>
        <!-- Leaflet JavaScript -->
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
                crossorigin=""></script>
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body { height: 100vh; overflow: hidden; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
            #map { height: 100vh; width: 100vw; position: relative; }
            .custom-popup { font-size: 14px; line-height: 1.4; }
            .error-message { background: #f8d7da; color: #721c24; padding: 20px; border-radius: 8px; text-align: center; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            (function() {
                try {
                    // Initialize map with bounds checking
                    const centerLat = Math.max(-85, Math.min(85, $centerLat));
                    const centerLng = Math.max(-180, Math.min(180, $centerLng));
                    const zoomLevel = Math.max(1, Math.min(18, $zoom));

                    const map = L.map('map', {
                        center: [centerLat, centerLng],
                        zoom: zoomLevel,
                        zoomControl: true,
                        attributionControl: true
                    });

                    // Add tile layer with error handling
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                        maxZoom: 19,
                        minZoom: 1,
                        errorTileUrl: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='
                    }).addTo(map);

                    // Add markers with custom styling
                    $currentUserMarker
                    $friendsMarkers
                    $placeMarker
                    $noDataMessage

                    // Log successful initialization
                    console.log('LocationTracker map initialized successfully with ${userLiveLocations.size} friends and current user location');

                    // Add map event listeners for debugging
                    map.on('load', function() {
                        console.log('Map tiles loaded successfully');
                    });

                } catch (error) {
                    console.error('LocationTracker map initialization failed:', error);
                    const mapDiv = document.getElementById('map');
                    if (mapDiv) {
                        mapDiv.innerHTML = '<div class="error-message"><strong>Map Loading Error</strong><br>Please check your internet connection and try again.</div>';
                    }
                }
            })();
        </script>
    </body>
    </html>
    """.trimIndent()
}

private fun escapeJsString(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", " ")
        .replace("\r", " ")
}
