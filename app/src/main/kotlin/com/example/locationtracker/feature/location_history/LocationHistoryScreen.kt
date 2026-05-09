package com.example.locationtracker.feature.location_history

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
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
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.locationtracker.ui.components.EmptyState
import com.example.locationtracker.domain.model.TrackedLocation
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.isSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = this
    val cal2 = Calendar.getInstance()
    cal2.time = other
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryScreen(navController: NavController, locationHistoryViewModel: LocationHistoryViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val uiState by locationHistoryViewModel.uiState.collectAsState()
    val trackedLocations = uiState.trackedLocations
    val selectedDate = uiState.selectedDate
    val searchStartDate = uiState.searchStartDate
    val searchEndDate = uiState.searchEndDate

    var selectedDayTracks by remember { mutableStateOf<List<TrackedLocation>>(emptyList()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val onStartDateSetListener = remember { DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        val newCal = Calendar.getInstance()
        newCal.time = searchStartDate ?: Date()
        newCal.set(year, month, dayOfMonth)
        locationHistoryViewModel.setSearchDateRange(newCal.time, searchEndDate)
        showStartDatePicker = false
        showStartTimePicker = true // Immediately show time picker after date
    }}

    val onStartTimeSetListener = remember { TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val newCal = Calendar.getInstance()
        newCal.time = searchStartDate ?: Date()
        newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        newCal.set(Calendar.MINUTE, minute)
        locationHistoryViewModel.setSearchDateRange(newCal.time, searchEndDate)
        showStartTimePicker = false
    }}

    val onEndDateSetListener = remember { DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        val newCal = Calendar.getInstance()
        newCal.time = searchEndDate ?: Date()
        newCal.set(year, month, dayOfMonth)
        locationHistoryViewModel.setSearchDateRange(searchStartDate, newCal.time)
        showEndDatePicker = false
        showEndTimePicker = true // Immediately show time picker after date
    }}

    val onEndTimeSetListener = remember { TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val newCal = Calendar.getInstance()
        newCal.time = searchEndDate ?: Date()
        newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        newCal.set(Calendar.MINUTE, minute)
        locationHistoryViewModel.setSearchDateRange(searchStartDate, newCal.time)
        showEndTimePicker = false
    }}

    val currentCalendar = remember { Calendar.getInstance() }

    if (showStartDatePicker) {
        DatePickerDialog(
            context,
            onStartDateSetListener,
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            context,
            onStartTimeSetListener,
            currentCalendar.get(Calendar.HOUR_OF_DAY),
            currentCalendar.get(Calendar.MINUTE),
            true // is24HourView
        ).show()
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            context,
            onEndDateSetListener,
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            context,
            onEndTimeSetListener,
            currentCalendar.get(Calendar.HOUR_OF_DAY),
            currentCalendar.get(Calendar.MINUTE),
            true // is24HourView
        ).show()
    }

    LaunchedEffect(trackedLocations, selectedDate) {
        selectedDayTracks = if (selectedDate != null) {
            trackedLocations.filter { it.timestamp != null && it.timestamp.isSameDay(selectedDate) }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            if (trackedLocations.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.LocationOff,
                    title = "No location history",
                    message = "Your location tracking history will appear here once you enable location sharing",
                    actionText = "Enable Sharing",
                    onActionClick = { navController.navigate("sharing") }
                )
            } else {
                // Map display area

                // Update camera to selected track's start or current location's last point

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            loadDataWithBaseURL(null, generateHistoryMapHtml(selectedDayTracks), "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, generateHistoryMapHtml(selectedDayTracks), "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Range Search UI
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Filter by Date Range:", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        ElevatedButton(
                            onClick = { showStartDatePicker = true },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text(text = "Start: ${searchStartDate?.let { dateFormat.format(it) } ?: "Select"}")
                        }
                        ElevatedButton(
                            onClick = { showEndDatePicker = true },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Text(text = "End: ${searchEndDate?.let { dateFormat.format(it) } ?: "Select"}")
                        }
                    }
                    Button(
                        onClick = { locationHistoryViewModel.setSearchDateRange(searchStartDate, searchEndDate) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Apply Time Filter", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Select a Day:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    val groupedByDay = trackedLocations.groupBy { it.timestamp?.let { date -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) } }
                        .toSortedMap(compareByDescending { it })

                    items(
                        items = groupedByDay.keys.toList(),
                        key = { it ?: "" }
                    ) { dateString ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    locationHistoryViewModel.setSelectedDate(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString!!))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Date: $dateString", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                val count = groupedByDay[dateString]?.size ?: 0
                                Text("Locations: $count", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateHistoryMapHtml(tracks: List<TrackedLocation>): String {
    if (tracks.isEmpty()) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Location History</title>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body { height: 100%; margin: 0; padding: 0; }
                #map { height: 100%; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([0, 0], 2);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    val start = tracks.first()
    val end = tracks.last()
    val path = tracks.joinToString(",") { "[${it.latitude}, ${it.longitude}]" }

    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>LocationTracker - History Map</title>
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
              integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
              crossorigin=""/>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
                crossorigin=""></script>
        <style>
            html, body {
                height: 100vh;
                margin: 0;
                padding: 0;
                font-family: 'Roboto', 'Helvetica Neue', Arial, sans-serif;
            }
            #map {
                height: 100vh;
                width: 100vw;
            }
            .start-marker {
                background-color: #28a745;
                border: 2px solid #fff;
                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
            }
            .end-marker {
                background-color: #dc3545;
                border: 2px solid #fff;
                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
            }
            .path-line {
                stroke: #007bff;
                stroke-width: 6;
                stroke-opacity: 0.8;
                fill: none;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            (function() {
                try {
                    // Create map centered on start location
                    const map = L.map('map').setView([${start.latitude}, ${start.longitude}], 14);

                    // Add tile layer
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                        maxZoom: 19
                    }).addTo(map);

                    // Add start marker with custom icon
                    const startIcon = L.divIcon({
                        className: 'start-marker',
                        html: '<div style="background:#28a745;color:white;padding:4px;border-radius:50%;width:24px;height:24px;display:flex;align-items:center;justify-content:center;font-weight:bold;">S</div>',
                        iconSize: [24, 24],
                        iconAnchor: [12, 12]
                    });

                    L.marker([${start.latitude}, ${start.longitude}], {icon: startIcon})
                        .addTo(map)
                        .bindPopup('<strong>Starting Point</strong><br/>Journey began here');

                    // Add end marker with custom icon
                    const endIcon = L.divIcon({
                        className: 'end-marker',
                        html: '<div style="background:#dc3545;color:white;padding:4px;border-radius:50%;width:24px;height:24px;display:flex;align-items:center;justify-content:center;font-weight:bold;">E</div>',
                        iconSize: [24, 24],
                        iconAnchor: [12, 12]
                    });

                    L.marker([${end.latitude}, ${end.longitude}], {icon: endIcon})
                        .addTo(map)
                        .bindPopup('<strong>Ending Point</strong><br/>Journey ended here');

                    // Add path polyline
                    const pathCoordinates = [$path];
                    L.polyline(pathCoordinates, {
                        color: '#007bff',
                        weight: 6,
                        opacity: 0.8,
                        lineCap: 'round',
                        lineJoin: 'round'
                    }).addTo(map);

                    // Fit map to show entire path
                    if (pathCoordinates.length > 1) {
                        map.fitBounds(L.polyline(pathCoordinates).getBounds(), {padding: [20, 20]});
                    }

                    console.log('LocationTracker history map loaded with ${tracks.size} points');

                } catch (error) {
                    console.error('History map error:', error);
                    document.getElementById('map').innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100vh;background:#f8f9fa;color:#6c757d;font-family:Arial,sans-serif;"><div><h3>Map Error</h3><p>Unable to load location history</p></div></div>';
                }
            })();
        </script>
    </body>
    </html>
    """.trimIndent()
}
