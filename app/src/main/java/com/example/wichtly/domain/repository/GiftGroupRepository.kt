package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.group.GiftGroup
import kotlinx.coroutines.flow.Flow

/**
 * Reine Persistenz für Wichtelgruppen. Fachliche Prüfungen passieren vorher im
 * SaveGiftGroupUseCase; geschrieben werden nur bereits geprüfte Gruppen.
 *
 * Lesen: `observe…` liefert einen Flow für Screens, die Änderungen live anzeigen;
 * `get…` liest den aktuellen Stand genau einmal (Use Cases, einmaliges Laden eines Formulars).
 */
interface GiftGroupRepository {
    fun observeGiftGroups(): Flow<List<GiftGroup>>
    fun observeGiftGroupById(id: String): Flow<GiftGroup?>

    /** Alle Gruppen, sortiert wie [observeGiftGroups]. */
    suspend fun getGiftGroups(): List<GiftGroup>

    /** @return null, wenn es keine Gruppe mit dieser ID gibt. */
    suspend fun getGiftGroupById(id: String): GiftGroup?

    /** Fotopfade aller Gruppen; für das Aufräumen verwaister Fotos. */
    suspend fun getAllPhotoPaths(): List<String>
    suspend fun insertGiftGroup(group: GiftGroup)

    /** @return false, wenn es keine Gruppe mit dieser ID (mehr) gibt. */
    suspend fun updateGiftGroup(group: GiftGroup): Boolean

    /**
     * Löscht die Gruppe. Teilnehmer, Wunschlisten und Auslosungen der Gruppe werden per CASCADE
     * mitgelöscht; Fotodateien räumt der DeleteGiftGroupUseCase auf.
     */
    suspend fun deleteGiftGroup(id: String)
}