package com.example.locationtracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.locationtracker.data.room.entity.FamilyEntity
import com.example.locationtracker.data.room.entity.LiveLocationEntity
import com.example.locationtracker.data.room.dao.FamilyDao
import com.example.locationtracker.data.room.dao.FriendRequestDao
import com.example.locationtracker.data.room.dao.LiveLocationDao
import com.example.locationtracker.data.room.dao.TrackedLocationDao
import com.example.locationtracker.data.room.dao.UserDao
import com.example.locationtracker.data.room.entity.FriendRequestEntity
import com.example.locationtracker.data.room.entity.TrackedLocationEntity
import com.example.locationtracker.data.room.entity.UserEntity
import com.example.locationtracker.data.room.util.StringListConverter
import com.example.locationtracker.data.room.util.DateConverter

@Database(
    entities = [
        TrackedLocationEntity::class,
        UserEntity::class,
        FriendRequestEntity::class,
        FamilyEntity::class,
        LiveLocationEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedLocationDao(): TrackedLocationDao
    abstract fun userDao(): UserDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun familyDao(): FamilyDao
    abstract fun liveLocationDao(): LiveLocationDao
}
