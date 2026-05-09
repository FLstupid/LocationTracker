package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class ToggleMasterSharingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(enable: Boolean) {
        userRepository.toggleMasterSharing(enable)
    }
}
