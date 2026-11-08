package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.validation.GiftGroupResult
import kotlinx.coroutines.flow.Flow


interface GiftGroupRepository {
    fun getGiftGroups(): Flow<List<GiftGroup>>
    fun getGiftGroupById(id: String): Flow<GiftGroup?>
    suspend fun createGiftGroup(group: GiftGroup): GiftGroupResult
    suspend fun updateGiftGroup(group: GiftGroup): GiftGroupResult
    suspend fun deleteGiftGroup(id: String)
}