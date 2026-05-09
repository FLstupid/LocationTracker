package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): List<User> {
        return userRepository.searchUsers(query)
    }
}
