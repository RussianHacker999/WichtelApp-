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

/**
 * Room access to the draw history: the draw itself, its assignments and the wishlist snapshots.
 *
 * A draw is written once and never changed; it is removed only through the CASCADE when its group is deleted.
 */
@Dao
interface DrawDao {
    /** All draws of a group, newest first. Feeds the history list, which shows only date and time. */
    @Query("SELECT * FROM draws WHERE groupId = :groupId ORDER BY drawnAt DESC")
    fun observeDrawsByGroupId(groupId: String): Flow<List<DrawEntity>>

    /**
     * Loads the group, its participants and their wishes for a draw in one transaction.
     *
     * Not a Flow, because the draw has to work on one fixed state of the data.
     * @return null if the group does not exist.
     */
    @Transaction
    @Query("SELECT * FROM gift_groups WHERE id = :groupId")
    suspend fun getDrawSource(groupId: String): GiftGroupWithParticipants?

    /**
     * Observes one draw with all assignments and their wishlist snapshots.
     *
     * Room re-runs the query whenever one of the three tables changes.
     * @return a Flow that emits null if the draw does not exist (any more).
     */
    @Transaction
    @Query("SELECT * FROM draws WHERE id = :drawId")
    fun observeDrawDetailById(drawId: String): Flow<DrawWithDetails?>

    /** Inserts the draw row. Fails with an exception if the ID already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDraw(draw: DrawEntity)

    /** Inserts the assignments of a draw. Fails with an exception if an ID already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignments(assignments: List<DrawAssignmentEntity>)

    /** Inserts the copied wishes of all receivers. Fails with an exception if an ID already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWishlistSnapshots(items: List<DrawWishlistItemSnapshotEntity>)

    /**
     * Saves a complete draw atomically: either the draw, all assignments and all snapshots are stored, or nothing is.
     *
     * The empty checks skip insert statements that would have nothing to write.
     */
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
