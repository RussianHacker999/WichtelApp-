package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar

/**
 * Stateless screen for the group detail view (group details and participant list).
 *
 * Content: [GroupDetailContent]; dialogs: [GroupDetailDialogs]; previews: GroupDetailScreenPreviews.kt.
 *
 * @param actions all user actions of the screen, see [GroupDetailActions].
 */
@Composable
fun GroupDetailScreen(state: GroupDetailUiState, actions: GroupDetailActions, modifier: Modifier = Modifier) {
    val group = state.group
    val ready = !state.isLoading && state.errorMessage == null && !state.notFound && group != null
    // The modifier is applied only to the top-level WichtlyScaffold.
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            WichtlyTopBar(
                title = stringResource(R.string.common_group_details),
                onBack = actions.onBack,
                actions = {
                    if (ready) {
                        ItemMenu(
                            actions = listOf(
                                MenuAction(stringResource(R.string.common_edit_group), Icons.Filled.Edit, actions.onEditGroupClick),
                                MenuAction(
                                    stringResource(R.string.group_delete),
                                    Icons.Filled.Delete,
                                    actions.onDeleteGroupClick,
                                    destructive = true,
                                ),
                            ),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (ready) {
                // "Draw" is the one main action of the screen (as in the group list).
                // Adding participants sits directly at the participant section.
                val enoughParticipants = DrawRules.canDraw(state.participants.size)
                val canDraw = enoughParticipants && !state.drawDayPassed
                val hint = when {
                    state.drawDayPassed -> stringResource(R.string.common_draw_day_passed)
                    !enoughParticipants -> stringResource(R.string.group_detail_min_participants_hint, DrawRules.MIN_PARTICIPANTS)
                    else -> null
                }
                BottomActionBar(hint = hint) {
                    PrimaryButton(
                        text = stringResource(R.string.common_draw),
                        onClick = actions.onPrepareDrawClick,
                        icon = Icons.Filled.Redeem,
                        enabled = canDraw,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            // 1. Loading
            state.isLoading -> LoadingState(text = stringResource(R.string.group_detail_loading))

            // 2. Loading error
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )

            // 3. Group not found
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )

            // Group was just deleted here and the route navigates back: show nothing
            group == null -> Unit

            // 4. Content
            else -> GroupDetailContent(
                group = group,
                participants = state.participants,
                participantPhotoFiles = state.participantPhotoFiles,
                innerPadding = innerPadding,
                onWishlistClick = actions.onWishlistClick,
                onAddParticipantClick = actions.onAddParticipantClick,
                onEditParticipantClick = actions.onEditParticipantClick,
                onDeleteParticipantClick = actions.onDeleteParticipantClick,
                onHistoryClick = actions.onHistoryClick,
            )
        }
    }

    // Dialogs as an overlay above the content
    GroupDetailDialogs(state = state, actions = actions)
}
