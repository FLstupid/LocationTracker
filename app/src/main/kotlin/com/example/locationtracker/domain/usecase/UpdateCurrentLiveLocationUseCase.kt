package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.LocationRepository
import javax.inject.Inject

class UpdateCurrentLiveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double) {
        locationRepository.addTrackedLocation(latitude, longitude)
    }
}
