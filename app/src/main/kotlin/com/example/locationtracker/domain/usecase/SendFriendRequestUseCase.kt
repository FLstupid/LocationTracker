package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class SendFriendRequestUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(toUid: String, fromPhone: String) {
        userRepository.sendFriendRequest(toUid, fromPhone)
    }
}
