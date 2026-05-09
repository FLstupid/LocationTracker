package com.example.locationtracker.data.repository

import com.example.locationtracker.data.datasource.PlacesDataSource
import com.example.locationtracker.domain.model.Place
import com.example.locationtracker.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesDataSource: PlacesDataSource
) : PlacesRepository {

    override fun getAutocompletePredictions(query: String): Flow<List<Place>> {
        return placesDataSource.getAutocompletePredictions(query)
    }
}
