package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateDisplayNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(displayName: String) {
        userRepository.updateDisplayName(displayName)
    }
}
