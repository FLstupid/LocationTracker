package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class CreateFamilyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): Boolean {
        return userRepository.createFamily(name)
    }
}
