package com.example.locationtracker.data.repository

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.locationtracker.data.datasource.PlacesDataSource
import com.example.locationtracker.domain.model.LatLng
import com.example.locationtracker.domain.model.Place
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PlacesDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PlacesDataSource {

    private val requestQueue by lazy { Volley.newRequestQueue(context) }

    override fun getAutocompletePredictions(query: String): Flow<List<Place>> = callbackFlow {
        // Validate query before making API call
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank() || trimmedQuery.length > 200 || trimmedQuery.contains(Regex("[\\n\\r]"))) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val url = "https://nominatim.openstreetmap.org/search?q=${java.net.URLEncoder.encode(trimmedQuery, "UTF-8")}&format=json&limit=5&addressdetails=1"

        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val places = mutableListOf<Place>()
                    for (i in 0 until response.length()) {
                        val item = response.getJSONObject(i)
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")
                        val displayName = item.getString("display_name")
                        val placeId = item.getString("place_id")
                        places.add(
                            Place(
                                id = placeId,
                                name = displayName.split(",")[0], // Simplified name
                                address = displayName,
                                latLng = LatLng(lat, lon)
                            )
                        )
                    }
                    trySend(places)
                } catch (e: Exception) {
                    Log.e("PlacesDataSource", "Failed to parse response: ${e.message}", e)
                    // Propagate error to ViewModel
                    close(e)
                }
            },
            { error ->
                Log.e("PlacesDataSource", "Failed to get predictions: ${error.message}", error)
                // Propagate error to ViewModel so UI can show message
                close(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // User-Agent is required by Nominatim Usage Policy
                headers["User-Agent"] = "LocationTrackerApp/1.0"
                return headers
            }
        }

        requestQueue.add(jsonArrayRequest)

        awaitClose { jsonArrayRequest.cancel() }
    }
}
