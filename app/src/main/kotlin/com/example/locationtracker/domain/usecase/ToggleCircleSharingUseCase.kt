package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class ToggleCircleSharingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(circleId: String, enable: Boolean) {
        userRepository.toggleCircleSharing(circleId, enable)
    }
}
