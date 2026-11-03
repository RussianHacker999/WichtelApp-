package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.wichtly.data.local.entity.GiftGroupEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface GiftGroupDao {

    @Query("SELECT * FROM gift_groups ORDER BY drawDay ASC")
    fun getAllGiftGroups(): Flow<List<GiftGroupEntity>>

    @Query("SELECT * FROM gift_groups WHERE id = :id")
    fun getGiftGroupById(id: String): Flow<GiftGroupEntity?>

    @Upsert
    suspend fun upsertGroup(group: GiftGroupEntity)

    @Query("DELETE FROM gift_groups WHERE id = :id")
    suspend fun deleteGroupById(id: String)
}
