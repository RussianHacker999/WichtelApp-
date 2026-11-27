package com.example.wichtly.ui.groups.list.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.controls.SectionActionButton
import com.example.wichtly.ui.components.display.ChipTone
import com.example.wichtly.ui.components.display.ListItemText
import com.example.wichtly.ui.components.display.PhotoThumbnail
import com.example.wichtly.ui.components.display.StatusChip
import com.example.wichtly.ui.components.display.areaSparkles
import com.example.wichtly.ui.components.display.formatCentsForInput
import com.example.wichtly.ui.components.display.initialsOf
import com.example.wichtly.ui.components.display.labelRes
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.BrandTopBar
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Stateless screen of the group list; the building blocks (top bar, cards, buttons) come from ui/components.
 *
 * The route connects the callbacks with the ViewModel and the navigation.
 *
 * @param onAddGroupClick "New group" was tapped.
 * @param onGroupClick a group card was tapped (selects it for "Draw").
 * @param onDetailsClick "Group details" in the card menu.
 * @param onEditGroupClick "Edit group" in the card menu.
 * @param onPrepareDrawClick "Draw" for the selected group.
 */
@Composable
fun GroupsScreen(
    state: GroupsUiState,
    onAddGroupClick: () -> Unit,
    onGroupClick: (groupId: String) -> Unit,
    onDetailsClick: (groupId: String) -> Unit,
    onEditGroupClick: (groupId: String) -> Unit,
    onPrepareDrawClick: (groupId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyScaffold(
        modifier = modifier,
        topBar = { BrandTopBar() },
        bottomBar = {
            // "Draw" is the one main action; "New group" sits at the "Groups" section.
            // Without groups (or while loading / on error) there is nothing to draw, so no action bar.
            if (!state.isLoading && state.errorMessage == null && state.groups.isNotEmpty()) {
                val hasSelection = state.selectedGroupId != null
                val hint = when {
                    !hasSelection -> stringResource(R.string.groups_select_hint)
                    state.selectedDrawDayPassed -> stringResource(R.string.common_draw_day_passed)
                    else -> null
                }
                BottomActionBar(hint = hint) {
                    PrimaryButton(
                        text = stringResource(R.string.common_draw),
                        // Without a selected card there is no valid target for the preparation.
                        enabled = hasSelection && !state.selectedDrawDayPassed,
                        onClick = { state.selectedGroupId?.let(onPrepareDrawClick) },
                        icon = Icons.Filled.Redeem,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // The draw button is not part of the list but fixed in the bottom action bar
            contentPadding = screenContentPadding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
        ) {
            item {
                PageHeader(title = stringResource(R.string.groups_title), subtitle = stringResource(R.string.groups_subtitle))
            }
            if (state.isLoading) {
                item {
                    LoadingState(
                        text = stringResource(R.string.common_loading_unpacking),
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                    )
                }
            } else if (state.errorMessage != null) {
                item {
                    MessageState(
                        title = stringResource(R.string.common_error_loading_title),
                        text = stringResource(state.errorMessage),
                        icon = Icons.Outlined.ErrorOutline,
                        isError = true,
                        fillMaxSize = false,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                    )
                }
            } else if (state.groups.isEmpty()) {
                item {
                    MessageState(
                        title = stringResource(R.string.groups_empty_title),
                        text = stringResource(R.string.groups_empty_text),
                        icon = Icons.Filled.Redeem,
                        actionLabel = stringResource(R.string.action_create_first_group),
                        onAction = onAddGroupClick,
                        fillMaxSize = false,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                    )
                }
            } else {
                item {
                    SectionHeader(
                        title = stringResource(R.string.groups_section),
                        count = state.groups.size,
                        action = { SectionActionButton(text = stringResource(R.string.groups_new), onClick = onAddGroupClick) },
                    )
                }
                items(state.groups, key = { it.id }) { group ->
                    HeroGroupCard(
                        group = group,
                        // null if the group has no photo or the file is missing
                        photoFile = state.photoFiles[group.id],
                        // The selected card gets a gold border and a check mark
                        selected = state.selectedGroupId == group.id,
                        onClick = { onGroupClick(group.id) },
                        onDetailsClick = { onDetailsClick(group.id) },
                        onEditClick = { onEditGroupClick(group.id) },
                    )
                }
            }
        }
    }
}

/**
 * Group card with a coloured image area (group photo, or a gradient with sparkles),
 * below it name, budget and draw type, and the ⋮ menu.
 */
@Composable
private fun HeroGroupCard(
    group: GiftGroup,
    photoFile: File?,
    selected: Boolean,
    onClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val chipDatePattern = stringResource(R.string.common_date_short_pattern)
    val chipDateFormatter = remember(chipDatePattern) { DateTimeFormatter.ofPattern(chipDatePattern) }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.border(BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary), shape) else Modifier),
        shape = shape,
        // The card colour comes from the colour scheme (surfaceContainer) instead of a fixed colour,
        // so all cards look the same and colours are changed in one place.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
        ) {
            if (photoFile != null) {
                // Fills the whole image area; the rounded corners come from the card
                PhotoThumbnail(
                    file = photoFile,
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                )
                // Without a photo file (no photo set or file missing): a gradient with sparkles and the initials
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                            ),
                        )
                        .areaSparkles(seed = group.id.hashCode()),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = Spacing.l, bottom = Spacing.m)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initialsOf(group.name),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 66.dp, y = (-12).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.common_selected),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            StatusChip(
                text = group.drawDay.format(chipDateFormatter),
                tone = ChipTone.OnImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.m),
            )
        }
        Row(
            modifier = Modifier.padding(start = Spacing.l, end = Spacing.tight, top = Spacing.inline, bottom = Spacing.inline),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItemText(
                title = group.name,
                subtitle = stringResource(
                    R.string.groups_card_subtitle,
                    formatCentsForInput(group.budgetCents),
                    stringResource(group.drawType.labelRes()),
                ),
                // Takes all the remaining width; the Row's padding provides the gap to the card edge
                modifier = Modifier.weight(1f),
            )
            ItemMenu(
                actions = listOf(
                    MenuAction(stringResource(R.string.common_group_details), Icons.Filled.Groups, onDetailsClick),
                    MenuAction(stringResource(R.string.common_edit_group), Icons.Filled.Edit, onEditClick),
                ),
            )
        }
    }
}

/** Preview: group list with several groups. */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun GroupsScreenPreview() {
    val groups = listOf(
        GiftGroup(id = "1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24)),
        GiftGroup(
            id = "2",
            name = "Büro Wichteln 2026",
            budgetCents = 1500,
            drawDay = LocalDate.of(2026, 12, 15),
            drawType = DrawType.JUNK,
        ),
        GiftGroup(id = "3", name = "Buchclub", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 8)),
    )
    WichtlyTheme {
        GroupsScreen(
            state = GroupsUiState(groups = groups, selectedGroupId = "1"),
            onAddGroupClick = {},
            onGroupClick = {},
            onDetailsClick = {},
            onEditGroupClick = {},
            onPrepareDrawClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun GroupsScreenEmptyPreview() {
    WichtlyTheme {
        GroupsScreen(
            state = GroupsUiState(),
            onAddGroupClick = {},
            onGroupClick = {},
            onDetailsClick = {},
            onEditGroupClick = {},
            onPrepareDrawClick = {},
        )
    }
}
