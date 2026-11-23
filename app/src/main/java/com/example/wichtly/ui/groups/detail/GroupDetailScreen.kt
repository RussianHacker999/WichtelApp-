package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.ChevronIcon
import com.example.wichtly.ui.components.ChipTone
import com.example.wichtly.ui.components.IconAvatar
import com.example.wichtly.ui.components.InitialsAvatar
import com.example.wichtly.ui.components.ItemMenu
import com.example.wichtly.ui.components.ListItemCard
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MenuAction
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.MetaItem
import com.example.wichtly.ui.components.MetaRow
import com.example.wichtly.ui.components.NoteCard
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SecondaryButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.StatTile
import com.example.wichtly.ui.components.StatusChip
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.groups.form.formatCentsForInput
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate

/**
 * Zustandsloser Screen der Gruppendetails (#3).
 *
 * Seitenkopf mit Wichteltag und Wichtelart, zwei Kennzahlen (Teilnehmer, Budget), die Teilnehmerliste
 * (Aktionen im ⋮-Menü jeder Karte) und der Bereich "Auslosung & Historie". Unten sitzen
 * "Teilnehmer hinzufügen" und "Losen". "Gruppe bearbeiten" liegt im ⋮-Menü der Top-Leiste.
 */
@Composable
fun GroupDetailScreen(
    state: GroupDetailUiState,
    onBack: () -> Unit,
    onEditGroupClick: () -> Unit,
    onOpenWishlist: (participantId: String) -> Unit,
    onAddParticipantClick: () -> Unit,
    onEditParticipantClick: (participantId: String) -> Unit,
    onDeleteParticipantClick: (participantId: String) -> Unit,
    onParticipantNameChange: (String) -> Unit,
    onSaveParticipant: () -> Unit,
    onDismissParticipantEditor: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onPhotoSelectClick: () -> Unit,
    onPhotoFlowClose: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
    onCameraDenied: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onPrepareDraw: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = state.group
    val ready = !state.isLoading && state.errorMessage == null && !state.notFound && group != null
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            WichtlyTopBar(
                title = "Gruppendetails",
                onBack = onBack,
                actions = {
                    if (ready) {
                        ItemMenu(actions = listOf(MenuAction("Gruppe bearbeiten", Icons.Filled.Edit, onEditGroupClick)))
                    }
                },
            )
        },
        bottomBar = {
            if (ready) {
                BottomActionBar {
                    SecondaryButton(
                        text = "Teilnehmer",
                        onClick = onAddParticipantClick,
                        icon = Icons.Filled.Add,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "Losen",
                        onClick = onPrepareDraw,
                        icon = Icons.Filled.Redeem,
                        modifier = Modifier.weight(1.4f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(text = "Gruppendetails werden geladen...")
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            state.notFound || group == null -> MessageState(
                title = "Diese Gruppe ist verschwunden",
                text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            else -> GroupDetailContent(
                group = group,
                participants = state.participants,
                participantPhotoFiles = state.participantPhotoFiles,
                innerPadding = innerPadding,
                onOpenWishlist = onOpenWishlist,
                onEditParticipantClick = onEditParticipantClick,
                onDeleteParticipantClick = onDeleteParticipantClick,
                onPrepareDraw = onPrepareDraw,
                onOpenHistory = onOpenHistory,
            )
        }
    }

    // Dialoge über dem Inhalt
    if (state.showParticipantEditor) {
        ParticipantEditorDialog(
            isEditMode = state.editingParticipantId != null,
            name = state.participantNameInput,
            errorMessage = state.participantNameError,
            isSaving = state.isSaving,
            onNameChange = onParticipantNameChange,
            onSave = onSaveParticipant,
            onDismiss = onDismissParticipantEditor,
            photoPath = state.participantPhotoPath,
            photoFile = state.participantPhotoFile,
            photoError = state.photoError,
            isPhotoFlowOpen = state.isPhotoFlowOpen,
            cameraDenied = state.cameraDenied,
            onPhotoSelectClick = onPhotoSelectClick,
            onPhotoFlowClose = onPhotoFlowClose,
            onPhotoCaptured = onPhotoCaptured,
            onRemovePhoto = onRemovePhoto,
            onCameraDenied = onCameraDenied,
            onOpenAppSettings = onOpenAppSettings,
        )
    }

    val pendingDeleteParticipant = state.pendingDeleteParticipantId?.let { id -> state.participants.find { it.id == id } }
    if (pendingDeleteParticipant != null) {
        ParticipantDeleteDialog(
            participantName = pendingDeleteParticipant.name,
            errorMessage = state.participantDeleteError,
            isSaving = state.isSaving,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

@Composable
private fun GroupDetailContent(
    group: GiftGroup,
    participants: List<Participant>,
    participantPhotoFiles: Map<String, File>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onOpenWishlist: (participantId: String) -> Unit,
    onEditParticipantClick: (participantId: String) -> Unit,
    onDeleteParticipantClick: (participantId: String) -> Unit,
    onPrepareDraw: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
    ) {
        item {
            PageHeader(
                eyebrow = "Gruppe",
                title = group.name,
                meta = {
                    MetaRow(
                        items = listOf(
                            MetaItem(Icons.Filled.Event, formatDateForInput(group.drawDay)),
                            MetaItem(Icons.Filled.Redeem, group.drawType.displayName),
                        )
                    )
                },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                StatTile(value = participants.size.toString(), label = "Teilnehmer", modifier = Modifier.weight(1f))
                StatTile(
                    value = "${formatCentsForInput(group.budgetCents)} €",
                    label = "Budget pro Geschenk",
                    modifier = Modifier.weight(1.7f),
                )
            }
        }

        item { SectionHeader(title = "Teilnehmer", count = participants.size) }
        if (participants.isEmpty()) {
            item {
                MessageState(
                    title = "Noch niemand dabei",
                    text = "Füge die Personen hinzu, die an der Wichtelrunde teilnehmen. Doppelte Namen sind erlaubt.",
                    icon = Icons.Filled.Groups,
                    fillMaxSize = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(participants, key = { it.id }) { participant ->
                ListItemCard(
                    title = participant.name,
                    subtitle = "Wunschliste öffnen",
                    leading = { InitialsAvatar(name = participant.name, photoFile = participantPhotoFiles[participant.id]) },
                    onClick = { onOpenWishlist(participant.id) },
                    trailing = {
                        ItemMenu(
                            contentDescription = "Aktionen für ${participant.name}",
                            actions = listOf(
                                MenuAction("Wunschliste öffnen", Icons.Filled.Redeem, { onOpenWishlist(participant.id) }),
                                MenuAction("Teilnehmer bearbeiten", Icons.Filled.Edit, { onEditParticipantClick(participant.id) }),
                                MenuAction("Teilnehmer löschen", Icons.Filled.Delete, { onDeleteParticipantClick(participant.id) }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }

        item { SectionHeader(title = "Auslosung & Historie") }
        item {
            val (statusText, tone) = when {
                participants.size < 2 -> "Teilnehmer fehlen" to ChipTone.Berry
                group.drawType == DrawType.NORMAL -> "Vorbereitung möglich" to ChipTone.Success
                else -> "Regeln offen" to ChipTone.Neutral
            }
            ListItemCard(
                title = "Neue Auslosung",
                subtitle = "Teilnehmer und Wichtelart prüfen, dann losen.",
                leading = { IconAvatar(icon = Icons.Filled.Redeem) },
                trailing = { StatusChip(text = statusText, tone = tone, modifier = Modifier.padding(end = Spacing.s)) },
                onClick = onPrepareDraw,
            )
        }
        item {
            ListItemCard(
                title = "Auslosungshistorie",
                subtitle = "Gespeicherte Durchläufe ansehen",
                leading = { IconAvatar(icon = Icons.Filled.History) },
                trailing = { ChevronIcon() },
                onClick = onOpenHistory,
            )
        }
        item {
            NoteCard(text = "Alte Ziehungen bleiben, wie sie waren.", modifier = Modifier.padding(top = Spacing.m))
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

private val previewGroup = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24))
private val previewParticipants = listOf("Mara", "Jonas Weiß", "Leonie").mapIndexed { i, n -> Participant(id = "p$i", groupId = "g1", name = n) }

@Composable
private fun PreviewScreen(state: GroupDetailUiState) {
    WichtlyTheme {
        GroupDetailScreen(
            state = state,
            onBack = {}, onEditGroupClick = {}, onOpenWishlist = {}, onAddParticipantClick = {},
            onEditParticipantClick = {}, onDeleteParticipantClick = {}, onParticipantNameChange = {},
            onSaveParticipant = {}, onDismissParticipantEditor = {}, onConfirmDelete = {}, onDismissDelete = {},
            onPhotoSelectClick = {}, onPhotoFlowClose = {}, onPhotoCaptured = {}, onRemovePhoto = {},
            onCameraDenied = {}, onOpenAppSettings = {}, onPrepareDraw = {}, onOpenHistory = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupDetailScreenContentWithParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = previewParticipants))
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenContentEmptyParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup.copy(drawType = DrawType.JUNK)))
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenLoadingPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = true))
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenNotFoundPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, notFound = true))
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenErrorPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, errorMessage = "Fehler beim Laden der Wichtelgruppe aus der Datenbank."))
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenWithParticipantEditorPreview() {
    PreviewScreen(
        GroupDetailUiState(
            isLoading = false, group = previewGroup, participants = previewParticipants,
            showParticipantEditor = true, participantNameInput = "Maria",
        )
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenWithParticipantDeleteDialogPreview() {
    PreviewScreen(
        GroupDetailUiState(
            isLoading = false, group = previewGroup, participants = previewParticipants,
            pendingDeleteParticipantId = "p0",
        )
    )
}
