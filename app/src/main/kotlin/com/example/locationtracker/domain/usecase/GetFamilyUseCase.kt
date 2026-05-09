package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.Family
import com.example.locationtracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<Family>> {
        return userRepository.getFamily()
    }
}
