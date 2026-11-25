package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.GiftGroup
import kotlinx.coroutines.flow.Flow

/**
 * Reine Persistenz für Wichtelgruppen. Fachliche Prüfungen passieren vorher im
 * SaveGiftGroupUseCase; geschrieben werden nur bereits geprüfte Gruppen.
 */
interface GiftGroupRepository {
    fun getGiftGroups(): Flow<List<GiftGroup>>
    fun getGiftGroupById(id: String): Flow<GiftGroup?>
    suspend fun insertGiftGroup(group: GiftGroup)

    /** @return false, wenn es keine Gruppe mit dieser ID (mehr) gibt. */
    suspend fun updateGiftGroup(group: GiftGroup): Boolean

    /**
     * Löscht die Gruppe. Teilnehmer, Wunschlisten und Auslosungen der Gruppe werden per CASCADE
     * mitgelöscht; Fotodateien räumt der DeleteGiftGroupUseCase auf.
     */
    suspend fun deleteGiftGroup(id: String)
}