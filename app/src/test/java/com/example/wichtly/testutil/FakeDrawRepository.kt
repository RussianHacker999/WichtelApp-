package com.example.wichtly.testutil

import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawSummary
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GroupDrawOverview
import com.example.wichtly.domain.model.ParticipantPreview
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.validation.DrawResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Ein In-Memory-Fake für [DrawRepository] zu Testzwecken.
 *
 * Unterstützt Test-Schalter wie [nextDrawResult] für erzwungene Ergebnisse, [shouldThrow] für
 * simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Ausführungen
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
 */
class FakeDrawRepository : DrawRepository {

    private val drawsFlow = MutableStateFlow<List<DrawDetail>>(emptyList())

    // Test-Schalter und Zähler
    var nextDrawResult: DrawResult? = null
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var performDrawCalls: Int = 0
        private set

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

    override fun observeGroupDrawOverview(): Flow<List<GroupDrawOverview>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Gruppen-Auslosungs-Übersicht") }
        }
        return flow { emit(emptyList()) }
    }

    override fun observeParticipantPreview(groupId: String): Flow<List<ParticipantPreview>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Teilnehmer-Vorschau") }
        }
        return flow { emit(emptyList()) }
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
                        drawType = draw.drawType,
                        assignmentCount = draw.assignments.size,
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

    override fun observeLatestDrawDetail(groupId: String): Flow<DrawDetail?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden des neuesten Durchlaufs") }
        }
        return drawsFlow.map { list ->
            list.filter { it.groupId == groupId }
                .maxByOrNull { it.drawnAt }
        }
    }

    override suspend fun performDraw(groupId: String): DrawResult {
        performDrawCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Fehler bei der Auslosung")
        }
        nextDrawResult?.let {
            nextDrawResult = null
            return it
        }

        val newDraw = DrawDetail(
            groupId = groupId,
            drawnAt = Instant.now(),
            drawType = DrawType.NORMAL,
            assignments = emptyList()
        )
        drawsFlow.value += newDraw
        return DrawResult.Success(newDraw)
    }
}
