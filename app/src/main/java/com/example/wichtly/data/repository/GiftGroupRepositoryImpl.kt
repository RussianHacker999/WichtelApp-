package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModels
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room implementation of [GiftGroupRepository]. Persistence only: validation happens
 * beforehand in SaveGiftGroupUseCase.
 */
class GiftGroupRepositoryImpl(private val giftGroupDao: GiftGroupDao) : GiftGroupRepository {

    override fun observeGiftGroups(): Flow<List<GiftGroup>> = giftGroupDao.observeGiftGroups().map { it.toDomainModels() }

    override fun observeGiftGroupById(id: String): Flow<GiftGroup?> = giftGroupDao.observeGiftGroupById(id).map { it?.toDomainModel() }

    override suspend fun getGiftGroups(): List<GiftGroup> = giftGroupDao.getGiftGroups().toDomainModels()

    override suspend fun getGiftGroupById(id: String): GiftGroup? = giftGroupDao.getGiftGroupById(id)?.toDomainModel()

    override suspend fun getAllPhotoPaths(): List<String> = giftGroupDao.getAllPhotoPaths()

    override suspend fun insertGiftGroup(group: GiftGroup) {
        giftGroupDao.insertGiftGroup(group.toEntity())
    }

    // @Update returns the number of updated rows; 0 means the group does not exist (any more)
    override suspend fun updateGiftGroup(group: GiftGroup): Boolean = giftGroupDao.updateGiftGroup(group.toEntity()) > 0

    override suspend fun deleteGiftGroup(id: String) {
        giftGroupDao.deleteGiftGroupById(id)
    }
}
