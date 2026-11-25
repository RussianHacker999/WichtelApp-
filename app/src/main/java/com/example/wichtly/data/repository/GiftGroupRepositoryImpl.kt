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
 * Room-Implementierung von [GiftGroupRepository]. Reine Persistenz: Prüfungen passieren
 * vorher im SaveGiftGroupUseCase.
 */
class GiftGroupRepositoryImpl(
    private val giftGroupDao: GiftGroupDao,
) : GiftGroupRepository {

    override fun observeGiftGroups(): Flow<List<GiftGroup>> {
        return giftGroupDao.observeGiftGroups().map { it.toDomainModels() }
    }

    override fun observeGiftGroupById(id: String): Flow<GiftGroup?> {
        return giftGroupDao.observeGiftGroupById(id).map { it?.toDomainModel() }
    }

    override suspend fun getGiftGroups(): List<GiftGroup> =
        giftGroupDao.getGiftGroups().toDomainModels()

    override suspend fun getGiftGroupById(id: String): GiftGroup? =
        giftGroupDao.getGiftGroupById(id)?.toDomainModel()

    override suspend fun getAllPhotoPaths(): List<String> = giftGroupDao.getAllPhotoPaths()

    override suspend fun insertGiftGroup(group: GiftGroup) {
        giftGroupDao.insertGiftGroup(group.toEntity())
    }

    // @Update liefert die Anzahl geänderter Zeilen; 0 heißt, die Gruppe existiert nicht (mehr)
    override suspend fun updateGiftGroup(group: GiftGroup): Boolean =
        giftGroupDao.updateGiftGroup(group.toEntity()) > 0

    override suspend fun deleteGiftGroup(id: String) {
        giftGroupDao.deleteGiftGroupById(id)
    }
}
