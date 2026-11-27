package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

/** Room access to the `participants` table. */
@Dao
interface ParticipantDao {

    /** Observes the participants of a group, sorted by name (case-insensitive). */
    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY name COLLATE NOCASE ASC")
    fun observeParticipantsByGroupId(groupId: String): Flow<List<ParticipantEntity>>

    /** Observes one participant; the Flow emits null if the participant does not exist (any more). */
    @Query("SELECT * FROM participants WHERE id = :id")
    fun observeParticipantById(id: String): Flow<ParticipantEntity?>

    /** Reads the participants of a group once, sorted like [observeParticipantsByGroupId]. */
    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY name COLLATE NOCASE ASC")
    suspend fun getParticipantsByGroupId(groupId: String): List<ParticipantEntity>

    /** Reads one participant once; returns null if the participant does not exist. */
    @Query("SELECT * FROM participants WHERE id = :id")
    suspend fun getParticipantById(id: String): ParticipantEntity?

    /** Relative paths of all participant photos. Used by the orphan photo cleanup. */
    @Query("SELECT photoPath FROM participants WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>

    /** Inserts a new participant. Fails with an exception if the ID already exists or the group does not exist. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipant(participant: ParticipantEntity)

    /**
     * Overwrites all columns of the participant with the same ID.
     *
     * @return number of updated rows; 0 means the participant does not exist (any more).
     */
    @Update
    suspend fun updateParticipant(participant: ParticipantEntity): Int

    /** Deletes the participant; CASCADE also deletes their wishes. Past draws are snapshots and stay unchanged. */
    @Query("DELETE FROM participants WHERE id = :id")
    suspend fun deleteParticipantById(id: String)
}
