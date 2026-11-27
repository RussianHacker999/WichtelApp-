package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.GiftGroupEntity
import kotlinx.coroutines.flow.Flow

/** Room access to the `gift_groups` table. */
@Dao
interface GiftGroupDao {

    /** Observes all groups, sorted by draw day and then by name (case-insensitive). */
    @Query("SELECT * FROM gift_groups ORDER BY drawDay ASC, name COLLATE NOCASE ASC")
    fun observeGiftGroups(): Flow<List<GiftGroupEntity>>

    /** Observes one group; the Flow emits null if the group does not exist (any more). */
    @Query("SELECT * FROM gift_groups WHERE id = :id")
    fun observeGiftGroupById(id: String): Flow<GiftGroupEntity?>

    /** Reads all groups once, sorted like [observeGiftGroups]. */
    @Query("SELECT * FROM gift_groups ORDER BY drawDay ASC, name COLLATE NOCASE ASC")
    suspend fun getGiftGroups(): List<GiftGroupEntity>

    /** Reads one group once; returns null if the group does not exist. */
    @Query("SELECT * FROM gift_groups WHERE id = :id")
    suspend fun getGiftGroupById(id: String): GiftGroupEntity?

    /** Relative paths of all group photos. Used by the orphan photo cleanup. */
    @Query("SELECT photoPath FROM gift_groups WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>

    /** Inserts a new group. Fails with an exception if the ID already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGiftGroup(group: GiftGroupEntity)

    /**
     * Overwrites all columns of the group with the same ID.
     *
     * @return number of updated rows; 0 means the group does not exist (any more).
     */
    @Update
    suspend fun updateGiftGroup(group: GiftGroupEntity): Int

    /** Deletes the group; CASCADE also deletes its participants, wishes and draws. */
    @Query("DELETE FROM gift_groups WHERE id = :id")
    suspend fun deleteGiftGroupById(id: String)
}
