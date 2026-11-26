package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.controls.PrimaryButton
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
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.MetaItem
import com.example.wichtly.ui.components.layout.MetaRow
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate


/**
 * Zustandsloser Composable-Screen für die Gruppen-Detailansicht (Gruppenstammdaten und Teilnehmerliste).
 *
 * @param state Der aktuelle UI-Zustand des Bildschirms.
 * @param actions Alle Nutzeraktionen des Screens, siehe [GroupDetailActions].
 * @param modifier Äußerer Modifier für den Screen.
 */
@Composable
fun GroupDetailScreen(
    state: GroupDetailUiState,
    actions: GroupDetailActions,
    modifier: Modifier = Modifier,
) {
    val group = state.group
    val ready = !state.isLoading && state.errorMessage == null && !state.notFound && group != null
    // Der übergebene modifier wird ausschließlich am Top-Level WichtlyScaffold angewendet.
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
                                MenuAction(stringResource(R.string.group_delete), Icons.Filled.Delete, actions.onDeleteGroupClick, destructive = true),
                            ),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (ready) {
                // "Losen" ist die EINE Hauptaktion des Screens (wie in der Gruppenliste).
                // Teilnehmer hinzufügen sitzt direkt am Teilnehmer-Abschnitt.
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
            // 1. ZUSTAND: Laden
            // UI-Darstellung für den Ladezustand (gemeinsamer LoadingState).
            state.isLoading -> LoadingState(text = stringResource(R.string.group_detail_loading))
            // 2. ZUSTAND: Fehler aufgetreten
            // UI-Darstellung im Fehlerfall (gemeinsamer MessageState).
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )
            // 3. ZUSTAND: Gruppe nicht gefunden
            // UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde (gemeinsamer MessageState).
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )
            // Gruppe wurde gerade hier gelöscht, die Route navigiert zurück: nichts mehr anzeigen
            group == null -> Unit
            // 4. ZUSTAND: Inhalt
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

    // Dialoge als Overlay über dem Inhalt
    val editor = state.participantEditor
    if (editor != null) {
        ParticipantEditorDialog(
            editor = editor,
            isBusy = state.isSaving,
            actions = actions.participantEditor,
        )
    }

    val pendingDeleteParticipant = state.pendingDeleteParticipantId?.let { id -> state.participants.find { it.id == id } }
    if (pendingDeleteParticipant != null) {
        ParticipantDeleteDialog(
            participantName = pendingDeleteParticipant.name,
            errorMessage = state.participantDeleteError?.let { stringResource(it) },
            isBusy = state.isSaving,
            onConfirm = actions.onConfirmParticipantDelete,
            onDismiss = actions.onDismissParticipantDelete,
        )
    }

    if (state.showGroupDeleteDialog && group != null) {
        GroupDeleteDialog(
            groupName = group.name,
            errorMessage = state.groupDeleteError?.let { stringResource(it) },
            isBusy = state.isDeletingGroup,
            onConfirm = actions.onConfirmGroupDelete,
            onDismiss = actions.onDismissGroupDelete,
        )
    }
}

/**
 * UI-Darstellung für den Inhalt der Gruppen-Detailansicht: Seitenkopf, Kennzahlen, Teilnehmerliste
 * (mit "Hinzufügen" im Abschnittskopf) und der Bereich "Historie".
 * Gelost wird ausschließlich über den Button "Losen" in der Aktionsleiste.
 *
 * @param onAddParticipantClick Teilnehmer-Dialog zum Hinzufügen öffnen.
 * @param onHistoryClick Historie öffnen.
 */
@Composable
private fun GroupDetailContent(
    group: GiftGroup,
    participants: List<Participant>,
    participantPhotoFiles: Map<String, File>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
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

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

private val previewActions = GroupDetailActions(
    onBack = { },
    onEditGroupClick = { },
    onWishlistClick = { _ -> },
    onAddParticipantClick = { },
    onEditParticipantClick = { _ -> },
    onDeleteParticipantClick = { _ -> },
    participantEditor = ParticipantEditorActions(onNameChange = { _ -> }, onSave = { }, onDismiss = { }, onPhotoCaptured = { }, onRemovePhoto = { }),
    onConfirmParticipantDelete = { },
    onDismissParticipantDelete = { },
    onPrepareDrawClick = { },
    onHistoryClick = { },
    onDeleteGroupClick = { },
    onConfirmGroupDelete = { },
    onDismissGroupDelete = { },
)

private val previewGroup = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24))
private val previewParticipants = listOf("Mara", "Jonas Weiß", "Leonie").mapIndexed { i, n -> Participant(id = "p$i", groupId = "g1", name = n) }

@Composable
private fun PreviewScreen(state: GroupDetailUiState) {
    WichtlyTheme {
        GroupDetailScreen(
            state = state,
            actions = previewActions,
        )
    }
}

/**
 * Preview 1: Inhalt mit 3 Teilnehmern (inkl. Initialen und 3-Punkte-Menü).
 */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupDetailScreenContentWithParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = previewParticipants))
}

/**
 * Preview 2: Inhalt mit leerer Teilnehmerliste.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenContentEmptyParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup.copy(drawType = DrawType.JUNK)))
}

/**
 * Preview 3: Ladezustand.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenLoadingPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = true))
}

/**
 * Preview 4: Gruppe nicht gefunden.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenNotFoundPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, notFound = true))
}

/**
 * Preview 5: Ladefehler auf Screen-Ebene.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenErrorPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, errorMessage = R.string.error_group_load))
}

/**
 * Preview 6: Screen mit geöffnetem Teilnehmer-Editor-Dialog.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenWithParticipantEditorPreview() {
    PreviewScreen(
        GroupDetailUiState(
            isLoading = false, group = previewGroup, participants = previewParticipants,
            participantEditor = ParticipantEditorUiState(nameInput = "Maria"),
        )
    )
}

/**
 * Preview 7: Screen mit geöffnetem Teilnehmer-Löschdialog.
 */
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

/**
 * Preview 8: Inhalt mit 12 Teilnehmern.
 */
@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun GroupDetailScreenContentManyParticipantsPreview() {
    val participants = (1..12).map { Participant(id = "p_$it", groupId = "g1", name = "Teilnehmer $it") }
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = participants))
}
