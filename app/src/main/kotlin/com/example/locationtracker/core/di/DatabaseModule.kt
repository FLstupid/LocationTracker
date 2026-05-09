package com.example.locationtracker.core.di

import android.content.Context
import androidx.room.Room
import com.example.locationtracker.data.room.AppDatabase
import com.example.locationtracker.data.room.dao.FamilyDao
import com.example.locationtracker.data.room.dao.FriendRequestDao
import com.example.locationtracker.data.room.dao.LiveLocationDao
import com.example.locationtracker.data.room.dao.TrackedLocationDao
import com.example.locationtracker.data.room.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "location-tracker-db"
        ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    @Singleton
    fun provideTrackedLocationDao(appDatabase: AppDatabase): TrackedLocationDao {
        return appDatabase.trackedLocationDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideFriendRequestDao(appDatabase: AppDatabase): FriendRequestDao {
        return appDatabase.friendRequestDao()
    }
    
    @Provides
    @Singleton
    fun provideFamilyDao(appDatabase: AppDatabase): FamilyDao {
        return appDatabase.familyDao()
    }
    
    @Provides
    @Singleton
    fun provideLiveLocationDao(appDatabase: AppDatabase): LiveLocationDao {
        return appDatabase.liveLocationDao()
    }
}
