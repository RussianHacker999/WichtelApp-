package com.example.wichtly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.wichtly.data.local.entity.DrawAssignmentEntity

import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawSummaryTuple
import com.example.wichtly.data.local.relation.DrawWithDetails
import kotlinx.coroutines.flow.Flow
@Dao
interface DrawDao {
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
    fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummaryTuple>>

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
