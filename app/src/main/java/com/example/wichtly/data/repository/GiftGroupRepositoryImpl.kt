package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.domain.validation.GiftGroupValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

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
        val groupWithId = if (group.id.isBlank()) {
            group.copy(id = UUID.randomUUID().toString())
        } else {
            group
        }
        val existingGroups = loadAllGroups()

        val errors = validator.validate(groupWithId, existingGroups, requireFutureDate = true)
        if (errors.isNotEmpty()) {
            return GiftGroupResult.ValidationError(errors)
        }
        giftGroupDao.insertGroup(groupWithId.toEntity())
        return GiftGroupResult.Success(groupWithId)
    }

    override suspend fun updateGiftGroup(group: GiftGroup): GiftGroupResult {
        val existingGroups = loadAllGroups()
        val errors = validator.validate(group, existingGroups, requireFutureDate = true)
        if (errors.isNotEmpty()) {
            return GiftGroupResult.ValidationError(errors)
        }

        val rowsUpdated = giftGroupDao.updateGroup(group.toEntity())
        if (rowsUpdated == 0) {
            return GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound))
        }
        return GiftGroupResult.Success(group)
    }




    override suspend fun deleteGiftGroup(id: String) {
        giftGroupDao.deleteGroupById(id)
    }

    private suspend fun loadAllGroups(): List<GiftGroup> =
        giftGroupDao.getAllGiftGroups().first().toDomainModelList()
}
