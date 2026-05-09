package com.example.locationtracker.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.locationtracker.data.room.entity.FamilyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM families ORDER BY updated_at DESC")
    fun getAllFamilies(): Flow<List<FamilyEntity>>

    @Query("SELECT * FROM families WHERE id = :familyId")
    fun getFamilyById(familyId: String): Flow<FamilyEntity?>

    @Query("SELECT * FROM families WHERE members LIKE '%' || :userId || '%'")
    fun getFamiliesByMember(userId: String): Flow<List<FamilyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilies(families: List<FamilyEntity>)

    @Query("DELETE FROM families WHERE id = :familyId")
    suspend fun deleteFamily(familyId: String)

    @Query("DELETE FROM families")
    suspend fun deleteAllFamilies()

    @Query("SELECT COUNT(*) FROM families")
    suspend fun getFamilyCount(): Int
}