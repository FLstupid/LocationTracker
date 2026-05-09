package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIncomingFriendRequestsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<FriendRequest>> {
        return userRepository.getIncomingFriendRequests()
    }
}
