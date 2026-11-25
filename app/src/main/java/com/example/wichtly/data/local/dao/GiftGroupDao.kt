package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wichtly.data.local.entity.GiftGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftGroupDao {

    @Query("SELECT * FROM gift_groups ORDER BY drawDay ASC, name COLLATE NOCASE ASC")
    fun observeGiftGroups(): Flow<List<GiftGroupEntity>>

    @Query("SELECT * FROM gift_groups WHERE id = :id")
    fun observeGiftGroupById(id: String): Flow<GiftGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGiftGroup(group: GiftGroupEntity)

    @Update
    suspend fun updateGiftGroup(group: GiftGroupEntity): Int

    @Query("DELETE FROM gift_groups WHERE id = :id")
    suspend fun deleteGiftGroupById(id: String)
}
