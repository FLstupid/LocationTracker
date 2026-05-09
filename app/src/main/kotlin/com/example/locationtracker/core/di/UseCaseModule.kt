package com.example.locationtracker.core.di

import com.example.locationtracker.domain.repository.LocationRepository
import com.example.locationtracker.domain.repository.PlacesRepository
import com.example.locationtracker.domain.repository.UserRepository
import com.example.locationtracker.domain.usecase.AcceptFriendRequestUseCase
import com.example.locationtracker.domain.usecase.CreateFamilyUseCase
import com.example.locationtracker.domain.usecase.GetAutocompletePredictionsUseCase
import com.example.locationtracker.domain.usecase.GetCurrentUserUseCase
import com.example.locationtracker.domain.usecase.GetFamilyUseCase
import com.example.locationtracker.domain.usecase.GetFriendsUseCase
import com.example.locationtracker.domain.usecase.GetIncomingFriendRequestsUseCase
import com.example.locationtracker.domain.usecase.GetLiveLocationUseCase
import com.example.locationtracker.domain.usecase.GetUsersUseCase
import com.example.locationtracker.domain.usecase.RejectFriendRequestUseCase
import com.example.locationtracker.domain.usecase.SearchUsersUseCase
import com.example.locationtracker.domain.usecase.SendFriendRequestUseCase
import com.example.locationtracker.domain.usecase.SignInUseCase
import com.example.locationtracker.domain.usecase.SignUpUseCase
import com.example.locationtracker.domain.usecase.ToggleCircleSharingUseCase
import com.example.locationtracker.domain.usecase.ToggleFriendSharingUseCase
import com.example.locationtracker.domain.usecase.ToggleMasterSharingUseCase
import com.example.locationtracker.domain.usecase.UpdateDisplayNameUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetLiveLocationUseCase(locationRepository: LocationRepository): GetLiveLocationUseCase {
        return GetLiveLocationUseCase(locationRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetCurrentUserUseCase(userRepository: UserRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetFamilyUseCase(userRepository: UserRepository): GetFamilyUseCase {
        return GetFamilyUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetUsersUseCase(userRepository: UserRepository): GetUsersUseCase {
        return GetUsersUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSignInUseCase(userRepository: UserRepository): SignInUseCase {
        return SignInUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSignUpUseCase(userRepository: UserRepository): SignUpUseCase {
        return SignUpUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCreateFamilyUseCase(userRepository: UserRepository): CreateFamilyUseCase {
        return CreateFamilyUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetFriendsUseCase(userRepository: UserRepository): GetFriendsUseCase {
        return GetFriendsUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetIncomingFriendRequestsUseCase(userRepository: UserRepository): GetIncomingFriendRequestsUseCase {
        return GetIncomingFriendRequestsUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSearchUsersUseCase(userRepository: UserRepository): SearchUsersUseCase {
        return SearchUsersUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSendFriendRequestUseCase(userRepository: UserRepository): SendFriendRequestUseCase {
        return SendFriendRequestUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideAcceptFriendRequestUseCase(userRepository: UserRepository): AcceptFriendRequestUseCase {
        return AcceptFriendRequestUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideRejectFriendRequestUseCase(userRepository: UserRepository): RejectFriendRequestUseCase {
        return RejectFriendRequestUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateDisplayNameUseCase(userRepository: UserRepository): UpdateDisplayNameUseCase {
        return UpdateDisplayNameUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideToggleFriendSharingUseCase(userRepository: UserRepository): ToggleFriendSharingUseCase {
        return ToggleFriendSharingUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideToggleCircleSharingUseCase(userRepository: UserRepository): ToggleCircleSharingUseCase {
        return ToggleCircleSharingUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideToggleMasterSharingUseCase(userRepository: UserRepository): ToggleMasterSharingUseCase {
        return ToggleMasterSharingUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetAutocompletePredictionsUseCase(placesRepository: PlacesRepository): GetAutocompletePredictionsUseCase {
        return GetAutocompletePredictionsUseCase(placesRepository)
    }
}
