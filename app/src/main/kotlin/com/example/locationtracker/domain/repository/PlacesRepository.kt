package com.example.locationtracker.domain.repository

import com.example.locationtracker.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getAutocompletePredictions(query: String): Flow<List<Place>>
}
