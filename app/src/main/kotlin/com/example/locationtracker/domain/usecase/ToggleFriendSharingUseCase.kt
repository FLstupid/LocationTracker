package com.example.locationtracker.domain.usecase

import com.example.locationtracker.domain.repository.UserRepository
import javax.inject.Inject

class ToggleFriendSharingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(friendUid: String, enable: Boolean) {
        userRepository.toggleFriendSharing(friendUid, enable)
    }
}
