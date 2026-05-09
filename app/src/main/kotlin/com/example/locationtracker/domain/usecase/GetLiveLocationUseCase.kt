package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.LiveLocation
import com.example.locationtracker.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(sharedUserIds: List<String>): Flow<Map<String, LiveLocation>> {
        return locationRepository.getLiveLocations(sharedUserIds)
    }
}
