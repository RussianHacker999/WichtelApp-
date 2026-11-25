package com.example.wichtly.testutil

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.model.draw.ParticipantWithWishlist
import com.example.wichtly.domain.repository.DrawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Ein In-Memory-Fake für [DrawRepository] zu Testzwecken.
 *
 * Reine Persistenz wie das echte Repository (gelost wird im PerformDrawUseCase).
 * Unterstützt Test-Schalter wie [shouldThrow] für simulierte Exceptions, [failFlows] für
 * fehlerhafte Lese-Flows sowie Zähler für Speichervorgänge
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
 */
class FakeDrawRepository : DrawRepository {

    private val drawsFlow = MutableStateFlow<List<DrawDetail>>(emptyList())

    // Test-Schalter und Zähler
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var insertDrawCalls: Int = 0
        private set

    private var groups: FakeGiftGroupRepository? = null
    private var participants: FakeParticipantRepository? = null
    private var wishlists: FakeWishlistRepository? = null

    /**
     * Verbindet das Fake mit den anderen Fakes, aus denen [getDrawSource] liest (wie die echte
     * Transaktion über die drei Tabellen). Gibt sich selbst zurück, damit der Aufruf inline passt.
     */
    fun withSources(
        groups: FakeGiftGroupRepository,
        participants: FakeParticipantRepository,
        wishlists: FakeWishlistRepository = FakeWishlistRepository(),
    ): FakeDrawRepository = apply {
        this.groups = groups
        this.participants = participants
        this.wishlists = wishlists
    }

    /**
     * Setzt die Liste der Auslosungsdurchläufe im Speicher direkt.
     */
    fun setDraws(draws: List<DrawDetail>) {
        drawsFlow.value = draws
    }

    /**
     * Fügt einen Auslosungsdurchlauf direkt zum Speicher hinzu.
     */
    fun addDraw(draw: DrawDetail) {
        drawsFlow.value += draw
    }

    /**
     * Entfernt einen Auslosungsdurchlauf direkt aus dem Speicher.
     */
    fun removeDraw(id: String) {
        drawsFlow.value = drawsFlow.value.filter { it.id != id }
    }

    override fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Auslosungshistorie") }
        }
        return drawsFlow.map { list ->
            list.filter { it.groupId == groupId }
                .sortedByDescending { it.drawnAt }
                .map { draw ->
                    DrawSummary(
                        id = draw.id,
                        groupId = draw.groupId,
                        drawnAt = draw.drawnAt,
                    )
                }
        }
    }

    override fun observeDrawDetailById(drawId: String): Flow<DrawDetail?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Auslosungsdetails") }
        }
        return drawsFlow.map { list -> list.find { it.id == drawId } }
    }

    override suspend fun getDrawSource(groupId: String): DrawSource? {
        val group = groups?.observeGiftGroupById(groupId)?.first() ?: return null
        val members = participants?.observeParticipantsByGroupId(groupId)?.first().orEmpty()
        return DrawSource(
            group = group,
            participants = members.sortedBy { it.name.lowercase() }.map { participant ->
                ParticipantWithWishlist(
                    participant = participant,
                    wishlist = wishlists?.observeWishlistItems(participant.id)?.first().orEmpty().sortedBy { it.sortOrder },
                )
            },
        )
    }

    override suspend fun insertDraw(draw: DrawDetail) {
        insertDrawCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Fehler beim Speichern der Auslosung")
        }
        drawsFlow.value += draw
    }
}
