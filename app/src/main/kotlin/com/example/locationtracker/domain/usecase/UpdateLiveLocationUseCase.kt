package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.LocationRepository
import javax.inject.Inject

class UpdateLiveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double) {
        locationRepository.updateLiveLocation(latitude, longitude)
    }
}
