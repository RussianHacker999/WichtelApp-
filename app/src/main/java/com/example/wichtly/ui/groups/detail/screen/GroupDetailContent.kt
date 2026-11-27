package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.controls.SectionActionButton
import com.example.wichtly.ui.components.display.ChevronIcon
import com.example.wichtly.ui.components.display.IconAvatar
import com.example.wichtly.ui.components.display.InitialsAvatar
import com.example.wichtly.ui.components.display.ListItemCard
import com.example.wichtly.ui.components.display.NoteCard
import com.example.wichtly.ui.components.display.StatTile
import com.example.wichtly.ui.components.display.formatCentsForInput
import com.example.wichtly.ui.components.display.formatDateForInput
import com.example.wichtly.ui.components.display.labelRes
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.MetaItem
import com.example.wichtly.ui.components.layout.MetaRow
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate


/**
 * Content of the group detail screen: page header, metrics, participant list
 * (with "Add" in the section header) and the "History" section.
 *
 * Drawing happens only through the "Draw" button in the action bar.
 */
@Composable
internal fun GroupDetailContent(
    group: GiftGroup,
    participants: List<Participant>,
    participantPhotoFiles: Map<String, File>,
    innerPadding: PaddingValues,
    onWishlistClick: (participantId: String) -> Unit,
    onAddParticipantClick: () -> Unit,
    onEditParticipantClick: (participantId: String) -> Unit,
    onDeleteParticipantClick: (participantId: String) -> Unit,
    onHistoryClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
    ) {
        item {
            // 1. Page header with eyebrow and title (group name)
            PageHeader(
                eyebrow = stringResource(R.string.common_group),
                title = group.name,
                // Group details (draw day and draw type) as one row; the budget is in the metrics below,
                // "Edit group" is in the ⋮ menu of the top bar
                meta = {
                    MetaRow(
                        items = listOf(
                            MetaItem(Icons.Filled.Event, formatDateForInput(group.drawDay)),
                            MetaItem(Icons.Filled.Redeem, stringResource(group.drawType.labelRes())),
                        )
                    )
                },
            )
        }
        item {
            // 2. Metrics: participants and budget
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                StatTile(value = participants.size.toString(), label = stringResource(R.string.common_participants), modifier = Modifier.weight(1f))
                StatTile(
                    value = stringResource(R.string.common_amount_euro, formatCentsForInput(group.budgetCents)),
                    label = stringResource(R.string.group_detail_budget_label),
                    modifier = Modifier.weight(1.7f),
                )
                // No third tile for draws: saved draws are listed in the draw history
            }
        }

        // 3. Header of the participant section with the count
        item {
            SectionHeader(
                title = stringResource(R.string.common_participants),
                count = participants.size,
                action = { SectionActionButton(text = stringResource(R.string.group_detail_add), onClick = onAddParticipantClick) },
            )
        }
        // 4. Hint for an empty list, or participant cards with the ⋮ menu
        if (participants.isEmpty()) {
            item {
                MessageState(
                    title = stringResource(R.string.group_detail_empty_title),
                    text = stringResource(R.string.group_detail_empty_text),
                    icon = Icons.Filled.Groups,
                    fillMaxSize = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(participants, key = { it.id }) { participant ->
                // The wishlist opens only by tapping the card (exactly one way); edit and delete are in the ⋮ menu.
                ListItemCard(
                    title = participant.name,
                    subtitle = stringResource(R.string.group_detail_open_wishlist),
                    leading = { InitialsAvatar(name = participant.name, photoFile = participantPhotoFiles[participant.id]) },
                    onClick = { onWishlistClick(participant.id) },
                    trailing = {
                        ItemMenu(
                            contentDescription = stringResource(R.string.group_detail_participant_actions, participant.name),
                            actions = listOf(
                                MenuAction(stringResource(R.string.participant_edit), Icons.Filled.Edit, { onEditParticipantClick(participant.id) }),
                                MenuAction(stringResource(R.string.participant_delete), Icons.Filled.Delete, { onDeleteParticipantClick(participant.id) }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }

        // 5. History section (drawing happens only via "Draw" in the action bar)
        item { SectionHeader(title = stringResource(R.string.group_detail_history_section)) }
        item {
            ListItemCard(
                title = stringResource(R.string.draw_history_title),
                leading = { IconAvatar(icon = Icons.Filled.History) },
                trailing = { ChevronIcon() },
                onClick = onHistoryClick,
            )
        }
        item {
            NoteCard(text = stringResource(R.string.group_detail_history_note), modifier = Modifier.padding(top = Spacing.m))
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupDetailContentPreview() {
    WichtlyTheme {
        GroupDetailContent(
            group = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24)),
            participants = listOf("Mara", "Jonas Weiß").mapIndexed { i, n -> Participant(id = "p$i", groupId = "g1", name = n) },
            participantPhotoFiles = emptyMap(),
            innerPadding = PaddingValues(),
            onWishlistClick = { _ -> },
            onAddParticipantClick = { },
            onEditParticipantClick = { _ -> },
            onDeleteParticipantClick = { _ -> },
            onHistoryClick = { },
        )
    }
}
