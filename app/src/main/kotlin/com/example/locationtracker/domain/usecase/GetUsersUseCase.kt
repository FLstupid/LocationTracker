package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.User
import com.example.locationtracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uids: List<String>): Flow<List<User>> {
        return userRepository.getUsers(uids)
    }
}
