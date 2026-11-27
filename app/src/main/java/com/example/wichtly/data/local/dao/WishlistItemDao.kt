package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

/** Room access to the `wishlist_items` table. */
@Dao
interface WishlistItemDao {

    /** Observes the wishes of a participant in their display order. */
    @Query("SELECT * FROM wishlist_items WHERE participantId = :participantId ORDER BY sortOrder ASC")
    fun observeWishlistItemsByParticipantId(participantId: String): Flow<List<WishlistItemEntity>>

    /** Reads the wishes of a participant once, in their display order. */
    @Query("SELECT * FROM wishlist_items WHERE participantId = :participantId ORDER BY sortOrder ASC")
    suspend fun getWishlistItemsByParticipantId(participantId: String): List<WishlistItemEntity>

    /** Inserts a new wish. Fails with an exception if the ID already exists or the participant does not exist. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    /**
     * Overwrites all columns of the wish with the same ID.
     *
     * @return number of updated rows; 0 means the wish does not exist (any more).
     */
    @Update
    suspend fun updateWishlistItem(item: WishlistItemEntity): Int

    /** @return number of deleted rows; 0 means the wish did not exist (any more). */
    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItemById(id: String): Int
}
