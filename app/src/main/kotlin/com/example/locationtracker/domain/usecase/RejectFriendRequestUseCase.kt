package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.model.FriendRequest
import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class RejectFriendRequestUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(request: FriendRequest) {
        userRepository.rejectFriendRequest(request)
    }
}
