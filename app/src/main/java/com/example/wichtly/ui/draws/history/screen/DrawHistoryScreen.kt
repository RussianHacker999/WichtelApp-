package com.example.wichtly.ui.draws.history.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.display.ChevronIcon
import com.example.wichtly.ui.components.display.IconAvatar
import com.example.wichtly.ui.components.display.ListItemCard
import com.example.wichtly.ui.components.display.NoteCard
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
 * Stateless screen of the draw history of a gift group.
 *
 * One [LazyColumn] scrolls the page header, one card per draw (date and time, opens the draw on tap),
 * the empty state ("Nothing has been drawn here yet.") and a note that saved draws do not change.
 *
 * @param onDrawClick opens a specific draw (receives the drawId).
 */
@Composable
fun DrawHistoryScreen(
    state: DrawHistoryUiState,
    onBack: () -> Unit,
    onDrawClick: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Date and time of a draw, e.g. "01.12.2026, 18:42"; the pattern lives in strings.xml
    val dateTimePattern = stringResource(R.string.common_datetime_pattern)
    val historyDateTimeFormatter = remember(dateTimePattern) { DateTimeFormatter.ofPattern(dateTimePattern) }
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = stringResource(R.string.draw_history_title), onBack = onBack) },
    ) { innerPadding ->
        when {
            // 1. Loading
            state.isLoading -> LoadingState(text = stringResource(R.string.draw_history_loading))

            // 2. Loading error
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )

            // 3. Group not found
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )

            // 4. Content: one LazyColumn, so page header, cards and notes scroll together
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    // (1) Page header with eyebrow and title
                    PageHeader(
                        eyebrow = stringResource(R.string.draw_history_eyebrow),
                        title = stringResource(R.string.draw_history_header),
                        subtitle = stringResource(R.string.draw_history_subtitle),
                    )
                }
                // (2) Section row with the count (number in the gold badge)
                item { SectionHeader(title = stringResource(R.string.draw_history_section), count = state.draws.size) }
                // (3) Draw cards, or a hint for an empty list
                if (state.draws.isEmpty()) {
                    item {
                        MessageState(
                            title = stringResource(R.string.draw_history_empty_title),
                            text = stringResource(R.string.draw_history_empty_text),
                            icon = Icons.Filled.History,
                            fillMaxSize = false,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                        )
                    }
                } else {
                    items(state.draws, key = { it.id }) { draw ->
                        // Only date and time: that is enough to tell several draws on the same day apart.
                        ListItemCard(
                            title = draw.drawnAt.format(historyDateTimeFormatter),
                            leading = { IconAvatar(icon = Icons.Filled.Redeem) },
                            trailing = { ChevronIcon() },
                            onClick = { onDrawClick(draw.id) },
                        )
                    }
                }
                item {
                    // (4) Note at the end
                    NoteCard(
                        text = stringResource(R.string.draw_history_note),
                        modifier = Modifier.padding(top = Spacing.m),
                    )
                }
            }
        }
    }
}

/**
 * Preview: history with three saved draws.
 */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DrawHistoryScreenWithThreeDrawsPreview() {
    val draws = listOf(
        DrawHistoryItemUiModel(id = "d1", drawnAt = LocalDateTime.of(2026, 12, 1, 18, 42)),
        DrawHistoryItemUiModel(id = "d2", drawnAt = LocalDateTime.of(2026, 11, 28, 20, 5)),
        DrawHistoryItemUiModel(id = "d3", drawnAt = LocalDateTime.of(2025, 12, 3, 19, 30)),
    )
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Familie Wichteln", draws = draws), onBack = {
        }, onDrawClick = {})
    }
}

/**
 * Preview: history with exactly one saved draw (the counter shows 1).
 */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DrawHistoryScreenWithSingleDrawPreview() {
    val draws = listOf(DrawHistoryItemUiModel(id = "d1", drawnAt = LocalDateTime.of(2026, 12, 1, 18, 42)))
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Familie Wichteln", draws = draws), onBack = {
        }, onDrawClick = {})
    }
}

/**
 * Preview: empty history (no draws yet).
 */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DrawHistoryScreenEmptyPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Büro Wichteln 2026"), onBack = {}, onDrawClick = {})
    }
}

/**
 * Preview: loading state.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenLoadingPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = true), onBack = {}, onDrawClick = {})
    }
}

/**
 * Preview: loading error.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenErrorPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(isLoading = false, errorMessage = R.string.error_history_load),
            onBack = {},
            onDrawClick = {},
        )
    }
}

/**
 * Preview: group not found.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenNotFoundPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, notFound = true), onBack = {}, onDrawClick = {})
    }
}
