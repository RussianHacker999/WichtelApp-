package com.example.wichtly.ui.draws.detail.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.components.controls.WichtlyTextButton
import com.example.wichtly.ui.components.display.ListItemCard
import com.example.wichtly.ui.components.display.NumberAvatar
import com.example.wichtly.ui.components.display.labelRes
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Stateless screen showing the result of a saved draw.
 *
 * Secrecy: the name of the receiver must not appear anywhere in the list, neither in the visible
 * text nor in accessibility/semantics properties. Every card shows only the giver's name, the
 * placeholder "gives to ???" and "Peek". The gift tag lies as an overlay above the screen content,
 * outside the state branches.
 *
 * @param onGiverClick selects a giver (receives the giverId) and opens the gift tag.
 * @param onDismissResult closes the gift tag with the result.
 */
@Composable
fun DrawDetailScreen(
    state: DrawDetailUiState,
    onBack: () -> Unit,
    onGiverClick: (giverId: String) -> Unit,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draw = state.draw
    // Date and time of the draw, e.g. "01.12.2026, 18:42"; same pattern as in the history
    val dateTimePattern = stringResource(R.string.common_datetime_pattern)
    val dateTimeFormatter = remember(dateTimePattern) { DateTimeFormatter.ofPattern(dateTimePattern) }
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            val title = when {
                draw == null -> stringResource(R.string.draw_detail_title_fallback)
                else -> draw.groupName ?: stringResource(R.string.draw_detail_group_placeholder)
            }
            WichtlyTopBar(title = title, onBack = onBack)
        },
    ) { innerPadding ->
        when {
            // 1. Loading
            state.isLoading -> LoadingState(text = stringResource(R.string.draw_detail_loading))
            // 2. Loading error
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back),
                onAction = onBack,
            )
            // 3. Draw not found
            state.notFound || draw == null -> MessageState(
                title = stringResource(R.string.draw_detail_not_found_title),
                text = stringResource(R.string.draw_detail_not_found_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back),
                onAction = onBack,
            )
            // 4. Content: one LazyColumn with page header, section row and the assignment cards
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    // (1) Page header with eyebrow (date) and title
                    PageHeader(
                        eyebrow = stringResource(R.string.draw_detail_eyebrow, draw.drawnAt.format(dateTimeFormatter)),
                        title = stringResource(R.string.draw_detail_header),
                        subtitle = stringResource(
                            R.string.draw_detail_subtitle,
                            draw.groupName ?: stringResource(R.string.draw_detail_group_placeholder),
                            draw.assignments.size,
                            stringResource(draw.drawType.labelRes()),
                        ),
                    )
                }
                // (2) Section row with the count (number in the gold badge)
                item { SectionHeader(title = stringResource(R.string.draw_detail_section), count = draw.assignments.size) }
                // (3) One card per assignment (only the giver's name and "gives to ???", never the receiver's name)
                itemsIndexed(draw.assignments, key = { _, a -> a.id }) { index, assignment ->
                    ListItemCard(
                        title = assignment.giverName,
                        subtitle = stringResource(R.string.draw_detail_gives_hidden),
                        leading = { NumberAvatar(number = index + 1) },
                        trailing = {
                            WichtlyTextButton(text = stringResource(R.string.draw_detail_peek), onClick = { onGiverClick(assignment.giverId) })
                        },
                        onClick = { onGiverClick(assignment.giverId) },
                    )
                }
            }
        }
    }

    // Dialog overlay outside the state branches.
    // The result as a gift tag: starts closed ("For your eyes only"), after a tap "…, you give a present to"
    val pendingRevealedAssignment = state.revealedGiverId?.let { id -> draw?.assignments?.find { it.giverId == id } }
    if (pendingRevealedAssignment != null) {
        DrawResultDialog(
            assignment = pendingRevealedAssignment,
            onDismiss = onDismissResult,
        )
    }
}

/**
 * Creates sample data for the previews.
 */
private fun createSampleDraw(): DrawDetailUiModel {
    val names = listOf("Mara", "Jonas", "Leonie", "Sam", "Maria", "Emil")
    return DrawDetailUiModel(
        id = "d-preview-1",
        groupId = "g1",
        groupName = "Familie Wichteln",
        drawnAt = LocalDateTime.of(2026, 12, 1, 18, 42),
        drawType = DrawType.NORMAL,
        assignments = names.mapIndexed { index, name ->
            val receiverIndex = (index + 1) % names.size
            DrawAssignmentUiModel(
                id = "a_${index + 1}",
                giverId = "p_${index + 1}",
                giverName = name,
                receiverId = "p_${receiverIndex + 1}",
                receiverName = names[receiverIndex],
                wishes = listOf("Ein gutes Notizbuch", "Tee aus einer kleinen Manufaktur"),
            )
        },
    )
}

@Composable
private fun PreviewScreen(state: DrawDetailUiState) {
    WichtlyTheme {
        DrawDetailScreen(state = state, onBack = {}, onGiverClick = {}, onDismissResult = {})
    }
}

/**
 * Preview: draw detail with 6 assignments.
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithAssignmentsPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw()))
}

/**
 * Preview: draw detail with the closed gift tag ("For your eyes only").
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithResultDialogPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw(), revealedGiverId = "p_1"))
}

/**
 * Preview: loading state.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenLoadingPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = true))
}

/**
 * Preview: loading error.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenErrorPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, errorMessage = R.string.error_draw_load))
}

/**
 * Preview: draw not found.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenNotFoundPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, notFound = true))
}
