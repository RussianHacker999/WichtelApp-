package com.example.wichtly.ui.draws.detail.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.draws.detail.screen.DrawAssignmentUiModel
import com.example.wichtly.ui.draws.detail.screen.DrawDetailUiModel
import com.example.wichtly.ui.draws.detail.screen.DrawDetailUiState
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "DrawDetailViewModel"

/**
 * Local state of the draw detail screen: which gift tag is currently open.
 *
 * Loading errors are not stored here; they come from the Flow via asResult.
 */
private data class DrawDetailLocalState(val revealedGiverId: String? = null)

/**
 * ViewModel of the detail screen of a saved draw.
 *
 * The state is built reactively with [combine] from the draw Flow of the [DrawRepository]
 * (plus the group name from the [GiftGroupRepository]) and the local UI state.
 * [SharingStarted.WhileSubscribed] (5 seconds) keeps the Flow active across configuration changes such
 * as a rotation. The gift tag survives rotations because its state is held in the ViewModel.
 *
 * @param drawId ID of the saved draw.
 * @param giftGroupRepository supplies the name of the drawn group.
 */
class DrawDetailViewModel(
    private val drawId: String,
    private val drawRepository: DrawRepository,
    private val giftGroupRepository: GiftGroupRepository,
) : ViewModel() {

    private val localStateFlow = MutableStateFlow(DrawDetailLocalState())

    /**
     * The draw together with the name of its group.
     *
     * Deliberately not a snapshot: a draw always belongs to its group and is deleted with it
     * (foreign key with CASCADE). The data that affects the result is frozen (participant names,
     * assignments, wishes, draw type). The group name is only a heading and is therefore loaded fresh;
     * if the group is renamed, the draw shows the new name too. To freeze it later: add a column to
     * DrawEntity plus an AutoMigration (see AppDatabase).
     * If only loading the name fails, the draw stays visible and the UI shows the placeholder.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val drawWithGroupName: Flow<Pair<Result<DrawDetail?>, String?>> = drawRepository.observeDrawDetailById(drawId)
        .asResult(TAG, "Failed to load the draw")
        .flatMapLatest { drawResult ->
            val draw = drawResult.getOrNull()
            if (draw == null) {
                flowOf(drawResult to null)
            } else {
                giftGroupRepository.observeGiftGroupById(draw.groupId)
                    .map<_, String?> { group -> group?.name }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        AppLog.w(TAG, "Failed to load the group name", e)
                        emit(null)
                    }
                    .map { name -> drawResult to name }
            }
        }

    val uiState: StateFlow<DrawDetailUiState> = combine(
        drawWithGroupName,
        localStateFlow,
    ) { (drawResult, groupName), localState ->
        val domainDraw = drawResult.getOrNull()
        val loadError = if (drawResult.isFailure) R.string.error_draw_load else null
        val uiDraw = domainDraw?.let { mapDomainToUiModel(it, groupName) }
        val isNotFound = domainDraw == null && loadError == null

        // Check that the IDs from the dialog state exist in the current assignment list
        val currentAssignments = uiDraw?.assignments ?: emptyList()
        val isRevealedGiverPresent = (localState.revealedGiverId != null) &&
            currentAssignments.any { it.giverId == localState.revealedGiverId }

        val validRevealedGiverId = if (isRevealedGiverPresent) localState.revealedGiverId else null

        DrawDetailUiState(
            isLoading = false,
            draw = uiDraw,
            notFound = isNotFound,
            errorMessage = loadError,
            revealedGiverId = validRevealedGiverId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DrawDetailUiState(isLoading = true),
    )

    /**
     * Opens the gift tag for the selected giver if the ID exists in the assignments.
     *
     * The tag starts closed, which takes over the role of a separate confirmation.
     */
    fun onGiverClick(giverId: String) {
        val currentAssignments = uiState.value.draw?.assignments ?: emptyList()
        if (currentAssignments.any { it.giverId == giverId }) {
            localStateFlow.update { it.copy(revealedGiverId = giverId) }
        }
    }

    /**
     * Closes the result tag.
     */
    fun onDismissResult() {
        localStateFlow.update {
            it.copy(revealedGiverId = null)
        }
    }

    /**
     * Closes all dialogs when the app is stopped (ON_STOP), so nobody sees a result by accident.
     * The route does not call it for a configuration change such as a rotation.
     */
    fun onAppStopped() {
        localStateFlow.update { it.copy(revealedGiverId = null) }
    }

    /**
     * Maps a domain [DrawDetail] to the UI model [DrawDetailUiModel].
     *
     * Sorts assignments by giverName (case-insensitive), then by giverId, and wishes by sortOrder,
     * keeping only their text.
     *
     * @param groupName current name of the group, or null if it could not be loaded.
     */
    private fun mapDomainToUiModel(domainDraw: DrawDetail, groupName: String?): DrawDetailUiModel {
        val sortedAssignments = domainDraw.assignments
            .sortedWith(
                compareBy<DrawAssignment, String>(String.CASE_INSENSITIVE_ORDER) { it.giverName }
                    .thenBy { it.giverId },
            )
            .map { assignment ->
                val sortedWishes = assignment.receiverWishlistItems
                    .sortedBy { it.sortOrder }
                    .map { it.text }

                DrawAssignmentUiModel(
                    id = assignment.id,
                    giverId = assignment.giverId,
                    giverName = assignment.giverName,
                    receiverId = assignment.receiverId,
                    receiverName = assignment.receiverName,
                    wishes = sortedWishes,
                )
            }

        return DrawDetailUiModel(
            id = domainDraw.id,
            groupId = domainDraw.groupId,
            groupName = groupName,
            drawnAt = domainDraw.drawnAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
            drawType = domainDraw.drawType,
            assignments = sortedAssignments,
        )
    }

    companion object {
        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
         */
        fun factory(container: AppContainer, drawId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DrawDetailViewModel(
                    drawId = drawId,
                    drawRepository = container.drawRepository,
                    giftGroupRepository = container.giftGroupRepository,
                )
            }
        }
    }
}
