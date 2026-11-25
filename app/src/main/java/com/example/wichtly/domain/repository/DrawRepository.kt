package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository für durchgeführte Auslosungen und ihre unveränderlichen Snapshots.
 */
interface DrawRepository {

    /**
     * Beobachtet die Kurzfassungen aller Auslosungen einer Gruppe, neueste zuerst.
     * Verwendet von: Historie (DrawHistoryViewModel).
     */
    fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>>

    /**
     * Beobachtet eine Auslosung vollständig (inklusive Zuordnungen und Wunsch-Snapshots).
     * Verwendet von: Auslosungsdetail (DrawDetailViewModel).
     */
    fun observeDrawDetailById(drawId: String): Flow<DrawDetail?>

    /**
     * Lädt die Gruppe mit allen Teilnehmern und deren Wunschlisten in einer Transaktion; null, wenn es die
     * Gruppe nicht gibt. Verwendet von: PerformDrawUseCase.
     */
    suspend fun getDrawSource(groupId: String): DrawSource?

    /**
     * Speichert eine vollständige, bereits geprüfte Auslosung (Auslosung, Zuordnungen und Wunsch-Snapshots)
     * atomar als unveränderlichen Snapshot.
     * Verwendet von: PerformDrawUseCase.
     */
    suspend fun insertDraw(draw: DrawDetail)
}
