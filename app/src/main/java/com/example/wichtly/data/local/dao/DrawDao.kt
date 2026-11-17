package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.relation.GroupDrawOverviewTuple

import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishListItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawSummaryTuple
import com.example.wichtly.data.local.relation.DrawWithDetails
import com.example.wichtly.data.local.relation.ParticipantPreviewTuple
import kotlinx.coroutines.flow.Flow
@Dao
interface DrawDao {
    @Query(
        """
        SELECT p.id AS participantId, p.name AS name,
               (SELECT COUNT(*) FROM wishlist_items wi
                 WHERE wi.participantId = p.id) AS wishCount
          FROM participants p
         WHERE p.groupId = :groupId
         ORDER BY p.name COLLATE NOCASE ASC
        """
    )
    fun observeParticipantPreview(groupId: String): Flow<List<ParticipantPreviewTuple>>



    @Query(
        """
        SELECT g.id AS groupId,
               (SELECT COUNT(*) FROM participants p WHERE p.groupId = g.id) AS participantCount,
               (SELECT COUNT(*) FROM draws d WHERE d.groupId = g.id) AS drawCount
          FROM gift_groups g
        """
    )
    fun observeGroupDrawOverview(): Flow<List<GroupDrawOverviewTuple>>

    @Query(
        """
        SELECT 
            d.id AS id, 
            d.groupId AS groupId, 
            d.drawnAt AS drawnAt, 
            d.drawType AS drawType, 
            (SELECT COUNT(*) FROM draw_assignments a WHERE a.drawId = d.id) AS assignmentCount
        FROM draws d
        WHERE d.groupId = :groupId
        ORDER BY d.drawnAt DESC
        """
    )
    fun getDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummaryTuple>>

    @Transaction
    @Query("SELECT * FROM draws WHERE id = :drawId")
    fun getDrawDetailById(drawId: String): Flow<DrawWithDetails?>

    @Transaction
    @Query("SELECT * FROM draws WHERE groupId = :groupId ORDER BY drawnAt DESC LIMIT 1")
    fun getLatestDrawDetailByGroupId(groupId: String): Flow<DrawWithDetails?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDraw(draw: DrawEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignments(assignments: List<DrawAssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishlistSnapshots(items: List<DrawWishListItemSnapshotEntity>)

    @Transaction
    suspend fun insertCompleteDraw(
        draw: DrawEntity,
        assignments: List<DrawAssignmentEntity>,
        wishlistSnapshots: List<DrawWishListItemSnapshotEntity>,
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
