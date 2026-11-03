package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class GiftGroupRepositoryImpl(
    private val giftGroupDao: GiftGroupDao,
    private val validator: GiftGroupValidator
) : GiftGroupRepository {

    override fun getGiftGroups(): Flow<List<GiftGroup>> {
        return giftGroupDao.getAllGiftGroups().map { it.toDomainModelList() }
    }

    override fun getGiftGroupById(id: String): Flow<GiftGroup?> {
        return giftGroupDao.getGiftGroupById(id).map { it?.toDomainModel() }
    }

    override suspend fun createGiftGroup(group: GiftGroup): GiftGroupResult {

        val existingGroups = loadAllGroups()

        val errors = validator.validate(group, existingGroups, requireFutureDate = true)
        if (errors.isNotEmpty()) {
            return GiftGroupResult.ValidationError(errors)
        }
        giftGroupDao.upsertGroup(group.toEntity())
        return GiftGroupResult.Success(group)
    }

    override suspend fun updateGiftGroup(group: GiftGroup): GiftGroupResult {
        val existingGroups = loadAllGroups()
        val errors = validator.validate(group, existingGroups, requireFutureDate = false)
        if (errors.isNotEmpty()) {
            return GiftGroupResult.ValidationError(errors)
        }

        giftGroupDao.upsertGroup(group.toEntity())
        return GiftGroupResult.Success(group)
    }

    override suspend fun deleteGiftGroup(id: String) {
        giftGroupDao.deleteGroupById(id)
    }

    private suspend fun loadAllGroups(): List<GiftGroup> =
        giftGroupDao.getAllGiftGroups().first().toDomainModelList()
}
