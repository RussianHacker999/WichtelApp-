package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
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

    override fun getGiftGroups(): Flow<List<GiftGroup>> {
        return giftGroupDao.getAllGiftGroups().map { it.toDomainModelList() }
    }

    override fun getGiftGroupById(id: String): Flow<GiftGroup?> {
        return giftGroupDao.getGiftGroupById(id).map { it?.toDomainModel() }
    }

    override suspend fun insertGiftGroup(group: GiftGroup) {
        giftGroupDao.insertGroup(group.toEntity())
    }

    // @Update liefert die Anzahl geänderter Zeilen; 0 heißt, die Gruppe existiert nicht (mehr)
    override suspend fun updateGiftGroup(group: GiftGroup): Boolean =
        giftGroupDao.updateGroup(group.toEntity()) > 0

    override suspend fun deleteGiftGroup(id: String) {
        giftGroupDao.deleteGroupById(id)
    }
}
