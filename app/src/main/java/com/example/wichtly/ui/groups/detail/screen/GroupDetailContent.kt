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
 * UI-Darstellung für den Inhalt der Gruppen-Detailansicht: Seitenkopf, Kennzahlen, Teilnehmerliste
 * (mit "Hinzufügen" im Abschnittskopf) und der Bereich "Historie".
 * Gelost wird ausschließlich über den Button "Losen" in der Aktionsleiste.
 *
 * @param onAddParticipantClick Teilnehmer-Dialog zum Hinzufügen öffnen.
 * @param onHistoryClick Historie öffnen.
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
            // 1. Eyebrow & Titel (Gruppenname) im Seitenkopf
            PageHeader(
                eyebrow = stringResource(R.string.common_group),
                title = group.name,
                // Teil des Seitenkopfs: Stammdaten der Gruppe (Wichteltag und Wichtelart) als Zeile
                // (das Budget steht in den Kennzahlen darunter, "Gruppe bearbeiten" im ⋮-Menü der Top-Leiste)
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
            // 2. Kennzahlen (2 Stat-Karten: Teilnehmer und Budget)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                // Stat 1: Teilnehmer
                StatTile(value = participants.size.toString(), label = stringResource(R.string.common_participants), modifier = Modifier.weight(1f))
                // Stat 2: Budget
                StatTile(
                    value = stringResource(R.string.common_amount_euro, formatCentsForInput(group.budgetCents)),
                    label = stringResource(R.string.group_detail_budget_label),
                    modifier = Modifier.weight(1.7f),
                )
                // keine dritte Karte für Durchläufe: die gespeicherten Durchläufe stehen in der Auslosungshistorie
            }
        }

        // 3. Kopfbereich des Teilnehmer-Abschnitts
        // Kopfbereich für den Teilnehmer-Abschnitt mit Anzahl.
        item {
            SectionHeader(
                title = stringResource(R.string.common_participants),
                count = participants.size,
                action = { SectionActionButton(text = stringResource(R.string.group_detail_add), onClick = onAddParticipantClick) },
            )
        }
        // 4. Hinweis bei leerer Liste ODER Teilnehmerkarten mit 3-Punkte-Menü
        if (participants.isEmpty()) {
            item {
                // Hinweisansicht, wenn die Gruppe noch keine Teilnehmer hat.
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
                // Einzelne Karte für ein Gruppenmitglied in der Teilnehmerliste.
                // Zeigt entweder das Teilnehmerfoto (über [PhotoThumbnail]) oder einen Avatar mit den Namensinitialen,
                // sowie das Drei-Punkte-Aktionsmenü (⋮) für Bearbeiten & Löschen.
                // Die Wunschliste öffnet sich ausschließlich per Tippen auf die Karte (nur EIN Weg).
                ListItemCard(
                    title = participant.name,
                    subtitle = stringResource(R.string.group_detail_open_wishlist),
                    // Avatar mit Foto oder Initialen
                    // Die Initialen (bis zu 2 Buchstaben) berechnet initialsOf() in ui/components.
                    leading = { InitialsAvatar(name = participant.name, photoFile = participantPhotoFiles[participant.id]) },
                    // Klick auf die ganze Karte öffnet die Wunschliste
                    onClick = { onWishlistClick(participant.id) },
                    trailing = {
                        // Drei-Punkte-Aktionsmenü (⋮)
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

        // 5. Historie-Abschnitt (gelost wird nur über "Losen" in der Aktionsleiste)
        item { SectionHeader(title = stringResource(R.string.group_detail_history_section)) }
        item {
            // Auslosungshistorie Karte
            ListItemCard(
                title = stringResource(R.string.draw_history_title),
                leading = { IconAvatar(icon = Icons.Filled.History) },
                trailing = { ChevronIcon() },
                onClick = onHistoryClick,
            )
        }
        item {
            // Hinweisfeld
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
