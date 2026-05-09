package com.example.locationtracker.core.di

import android.content.Context
import com.example.locationtracker.core.notification.NotificationChannelManager
import com.example.locationtracker.core.notification.NotificationHelper
import com.example.locationtracker.core.utils.NetworkObserver
import com.example.locationtracker.data.datasource.LocalDataSource
import com.example.locationtracker.data.datasource.PlacesDataSource
import com.example.locationtracker.data.datasource.RemoteDataSource
import com.example.locationtracker.data.local.LocalDataSourceImpl
import com.example.locationtracker.data.local.preferences.PreferencesDataSource
import com.example.locationtracker.data.location.DefaultLocationClient
import com.example.locationtracker.data.remote.RemoteDataSourceImpl
import com.example.locationtracker.data.repository.ActivityRepositoryImpl
import com.example.locationtracker.data.repository.LocationRepositoryImpl
import com.example.locationtracker.data.repository.PlacesDataSourceImpl
import com.example.locationtracker.data.repository.PlacesRepositoryImpl
import com.example.locationtracker.data.repository.SettingsRepositoryImpl
import com.example.locationtracker.data.repository.StorageRepositoryImpl
import com.example.locationtracker.data.repository.UserRepositoryImpl
import com.example.locationtracker.data.room.dao.FamilyDao
import com.example.locationtracker.data.room.dao.FriendRequestDao
import com.example.locationtracker.data.room.dao.LiveLocationDao
import com.example.locationtracker.data.room.dao.TrackedLocationDao
import com.example.locationtracker.data.room.dao.UserDao
import com.example.locationtracker.domain.repository.ActivityRepository
import com.example.locationtracker.domain.repository.LocationClient
import com.example.locationtracker.domain.repository.LocationRepository
import com.example.locationtracker.domain.repository.PlacesRepository
import com.example.locationtracker.domain.repository.SettingsRepository
import com.example.locationtracker.domain.repository.StorageRepository
import com.example.locationtracker.domain.repository.UserRepository
import com.example.locationtracker.domain.usecase.UpdateLiveLocationUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationClient(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationClient {
        return DefaultLocationClient(context, fusedLocationProviderClient)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore): RemoteDataSource {
        return RemoteDataSourceImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(
        trackedLocationDao: TrackedLocationDao,
        userDao: UserDao,
        friendRequestDao: FriendRequestDao,
        familyDao: FamilyDao,
        liveLocationDao: LiveLocationDao
    ): LocalDataSource {
        return LocalDataSourceImpl(
            trackedLocationDao,
            userDao,
            friendRequestDao,
            familyDao,
            liveLocationDao
        )
    }

    @Provides
    @Singleton
    fun providePlacesDataSource(@ApplicationContext context: Context): PlacesDataSource {
        return PlacesDataSourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        networkObserver: NetworkObserver
    ): UserRepository {
        return UserRepositoryImpl(remoteDataSource, localDataSource, networkObserver)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        networkObserver: NetworkObserver
    ): LocationRepository {
        return LocationRepositoryImpl(remoteDataSource, localDataSource, networkObserver)
    }

    @Provides
    @Singleton
    fun providePlacesRepository(placesDataSource: PlacesDataSource): PlacesRepository {
        return PlacesRepositoryImpl(placesDataSource)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(preferencesDataSource: PreferencesDataSource): SettingsRepository {
        return SettingsRepositoryImpl(preferencesDataSource)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ): StorageRepository {
        return StorageRepositoryImpl(storage, auth)
    }

    @Provides
    @Singleton
    fun provideUpdateLiveLocationUseCase(locationRepository: LocationRepository): UpdateLiveLocationUseCase {
        return UpdateLiveLocationUseCase(locationRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideNotificationChannelManager(@ApplicationContext context: Context): NotificationChannelManager {
        return NotificationChannelManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        channelManager: NotificationChannelManager
    ): NotificationHelper {
        return NotificationHelper(context, channelManager)
    }

    @Provides
    @Singleton
    fun provideActivityRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        networkObserver: NetworkObserver
    ): ActivityRepository {
        return ActivityRepositoryImpl(firestore, auth, networkObserver)
    }
}
