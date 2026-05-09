package com.example.locationtracker.data.datasource

import com.example.locationtracker.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlacesDataSource {
    fun getAutocompletePredictions(query: String): Flow<List<Place>>
}
