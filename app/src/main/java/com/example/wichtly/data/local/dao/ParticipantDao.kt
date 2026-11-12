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

    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY name ASC")
    fun getParticipantsByGroupId(groupId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE id = :id")
    fun getParticipantById(id: String): Flow<ParticipantEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipant(participant: ParticipantEntity)

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity): Int

    @Query("DELETE FROM participants WHERE id = :id")
    suspend fun deleteParticipantById(id: String)
}
