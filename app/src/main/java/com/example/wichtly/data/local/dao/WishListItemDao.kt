package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.WishListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishListItemDao {

    @Query("SELECT * FROM wishlist_items WHERE participantId = :participantId ORDER BY sortOrder ASC")
    fun observeWishlistItemsByParticipantId(participantId: String): Flow<List<WishListItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    fun getWishListItemById(id: String): Flow<WishListItemEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishListItem(item: WishListItemEntity)

    @Update
    suspend fun updateWishListItem(item: WishListItemEntity): Int

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishListItemById(id: String)
}
