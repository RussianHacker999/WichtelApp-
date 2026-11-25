package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawWithDetails
import com.example.wichtly.data.local.relation.GiftGroupWithParticipants
import kotlinx.coroutines.flow.Flow
@Dao
interface DrawDao {
    /** Alle Auslosungen einer Gruppe, neueste zuerst. Für die Historienliste (nur Datum und Uhrzeit). */
    @Query("SELECT * FROM draws WHERE groupId = :groupId ORDER BY drawnAt DESC")
    fun observeDrawsByGroupId(groupId: String): Flow<List<DrawEntity>>

    /**
     * Lädt Gruppe, Teilnehmer und Wünsche für eine Auslosung in einer Transaktion.
     * Nicht als Flow, weil die Auslosung einen festen Stand braucht.
     */
    @Transaction
    @Query("SELECT * FROM gift_groups WHERE id = :groupId")
    suspend fun getDrawSource(groupId: String): GiftGroupWithParticipants?

    @Transaction
    @Query("SELECT * FROM draws WHERE id = :drawId")
    fun observeDrawDetailById(drawId: String): Flow<DrawWithDetails?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDraw(draw: DrawEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignments(assignments: List<DrawAssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishlistSnapshots(items: List<DrawWishlistItemSnapshotEntity>)

    @Transaction
    suspend fun insertCompleteDraw(
        draw: DrawEntity,
        assignments: List<DrawAssignmentEntity>,
        wishlistSnapshots: List<DrawWishlistItemSnapshotEntity>,
    ) {
        insertDraw(draw)
        if (assignments.isNotEmpty()) {
            insertAssignments(assignments)
        }
        if (wishlistSnapshots.isNotEmpty()) {
            insertWishlistSnapshots(wishlistSnapshots)
        }
    }
}
