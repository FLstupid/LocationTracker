package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Boolean {
        return userRepository.signIn(email, password)
    }
}
