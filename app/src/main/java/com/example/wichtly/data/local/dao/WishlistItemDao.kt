package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistItemDao {

    @Query("SELECT * FROM wishlist_items WHERE participantId = :participantId ORDER BY sortOrder ASC")
    fun observeWishlistItemsByParticipantId(participantId: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE participantId = :participantId ORDER BY sortOrder ASC")
    suspend fun getWishlistItemsByParticipantId(participantId: String): List<WishlistItemEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Update
    suspend fun updateWishlistItem(item: WishlistItemEntity): Int

    /** @return Anzahl gelöschter Zeilen; 0 heißt, den Wunsch gab es nicht (mehr). */
    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItemById(id: String): Int
}
