package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {

    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY name COLLATE NOCASE ASC")
    fun observeParticipantsByGroupId(groupId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT photoPath FROM participants WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>

    @Query("SELECT * FROM participants WHERE id = :id")
    fun observeParticipantById(id: String): Flow<ParticipantEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipant(participant: ParticipantEntity)

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity): Int

    @Query("DELETE FROM participants WHERE id = :id")
    suspend fun deleteParticipantById(id: String)
}
