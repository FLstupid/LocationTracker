package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.Place
import com.example.locationtracker.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAutocompletePredictionsUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    operator fun invoke(query: String): Flow<List<Place>> {
        return placesRepository.getAutocompletePredictions(query)
    }
}
