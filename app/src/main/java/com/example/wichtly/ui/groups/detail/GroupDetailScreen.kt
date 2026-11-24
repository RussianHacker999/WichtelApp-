package com.example.wichtly.ui.groups.detail

import com.example.wichtly.domain.model.DrawRules
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.ChevronIcon
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
import com.example.wichtly.ui.components.SectionActionButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.StatTile
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
 * Zustandsloser Composable-Screen für die Gruppen-Detailansicht (Gruppenstammdaten und Teilnehmerliste).
 *
 * @param state Der aktuelle UI-Zustand des Bildschirms.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onEditGroupClick Callback zum Bearbeiten der Stammdaten der Gruppe.
 * @param onOpenWishlist Callback zum Öffnen der Wunschliste eines Teilnehmers.
 * @param onAddParticipantClick Callback zum Hinzufügen eines neuen Teilnehmers.
 * @param onEditParticipantClick Callback zum Bearbeiten eines Teilnehmers (erhält participantId).
 * @param onDeleteParticipantClick Callback zum Löschen eines Teilnehmers (erhält participantId).
 * @param onParticipantNameChange Callback bei Änderung des Namen-Eingabefeldes im Teilnehmer-Dialog.
 * @param onSaveParticipant Callback zum Speichern eines Teilnehmers.
 * @param onDismissParticipantEditor Callback zum Schließen/Abbrechen des Teilnehmer-Editor-Dialogs.
 * @param onConfirmDelete Callback zum Bestätigen der Teilnehmer-Löschung.
 * @param onDismissDelete Callback zum Schließen/Abbrechen des Teilnehmer-Löschdialogs.
 * @param onPhotoSelectClick Callback zum Starten der Foto-Auswahl für Teilnehmer.
 * @param onPhotoFlowClose Callback zum Schließen des Foto-Workflows.
 * @param onPhotoCaptured Callback nach erfolgreicher Fotoaufnahme.
 * @param onRemovePhoto Callback zum Entfernen des Teilnehmerfotos.
 * @param onCameraDenied Callback bei verweigerter Kameraberechtigung.
 * @param onOpenAppSettings Callback zum Öffnen der Systemeinstellungen.
 * @param onPrepareDraw Auslosungs-Vorbereitung für diese Gruppe öffnen.
 * @param onOpenHistory Historie dieser Gruppe öffnen.
 * @param modifier Äußerer Modifier für den Screen.
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
    // Der übergebene modifier wird ausschließlich am Top-Level WichtlyScaffold angewendet.
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
                // "Losen" ist die EINE Hauptaktion des Screens (wie in der Gruppenliste).
                // Teilnehmer hinzufügen sitzt direkt am Teilnehmer-Abschnitt.
                val canDraw = DrawRules.canDraw(state.participants.size)
                BottomActionBar(hint = if (canDraw) null else "Mindestens ${DrawRules.MIN_PARTICIPANTS} Teilnehmer zum Losen") {
                    PrimaryButton(
                        text = "Losen",
                        onClick = onPrepareDraw,
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
            state.isLoading -> LoadingState(text = "Gruppendetails werden geladen...")
            // 2. ZUSTAND: Fehler aufgetreten
            // UI-Darstellung im Fehlerfall (gemeinsamer MessageState).
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            // 3. ZUSTAND: Gruppe nicht gefunden
            // UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde (gemeinsamer MessageState).
            state.notFound || group == null -> MessageState(
                title = "Diese Gruppe ist verschwunden",
                text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            // 4. ZUSTAND: Inhalt
            else -> GroupDetailContent(
                group = group,
                participants = state.participants,
                participantPhotoFiles = state.participantPhotoFiles,
                innerPadding = innerPadding,
                onOpenWishlist = onOpenWishlist,
                onAddParticipantClick = onAddParticipantClick,
                onEditParticipantClick = onEditParticipantClick,
                onDeleteParticipantClick = onDeleteParticipantClick,
                onOpenHistory = onOpenHistory,
            )
        }
    }

    // Dialoge als Overlay über dem Inhalt
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

/**
 * UI-Darstellung für den Inhalt der Gruppen-Detailansicht: Seitenkopf, Kennzahlen, Teilnehmerliste
 * (mit "Hinzufügen" im Abschnittskopf) und der Bereich "Historie".
 * Gelost wird ausschließlich über den Button "Losen" in der Aktionsleiste.
 *
 * @param onAddParticipantClick Teilnehmer-Dialog zum Hinzufügen öffnen.
 * @param onOpenHistory Historie öffnen.
 */
@Composable
private fun GroupDetailContent(
    group: GiftGroup,
    participants: List<Participant>,
    participantPhotoFiles: Map<String, File>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onOpenWishlist: (participantId: String) -> Unit,
    onAddParticipantClick: () -> Unit,
    onEditParticipantClick: (participantId: String) -> Unit,
    onDeleteParticipantClick: (participantId: String) -> Unit,
    onOpenHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
    ) {
        item {
            // 1. Eyebrow & Titel (Gruppenname) im Seitenkopf
            PageHeader(
                eyebrow = "Gruppe",
                title = group.name,
                // 3. Stammdaten der Gruppe: Wichteltag und Wichtelart als Zeile im Seitenkopf
                // (das Budget steht in den Kennzahlen darunter, "Gruppe bearbeiten" im ⋮-Menü der Top-Leiste)
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
            // 2. Kennzahlen (2 Stat-Karten: Teilnehmer und Budget)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                // Stat 1: Teilnehmer
                StatTile(value = participants.size.toString(), label = "Teilnehmer", modifier = Modifier.weight(1f))
                // Stat 2: Budget
                StatTile(
                    value = "${formatCentsForInput(group.budgetCents)} €",
                    label = "Budget pro Geschenk",
                    modifier = Modifier.weight(1.7f),
                )
                // keine dritte Karte für Durchläufe: die gespeicherten Durchläufe stehen in der Auslosungshistorie
            }
        }

        // 4. Kopfbereich des Teilnehmer-Abschnitts
        // Kopfbereich für den Teilnehmer-Abschnitt mit Anzahl.
        item {
            SectionHeader(
                title = "Teilnehmer",
                count = participants.size,
                action = { SectionActionButton(text = "Hinzufügen", onClick = onAddParticipantClick) },
            )
        }
        // 5. Hinweis bei leerer Liste ODER Teilnehmerkarten mit 3-Punkte-Menü
        if (participants.isEmpty()) {
            item {
                // Hinweisansicht, wenn die Gruppe noch keine Teilnehmer hat.
                MessageState(
                    title = "Noch niemand dabei",
                    text = "Füge die Personen hinzu, die an der Wichtelrunde teilnehmen.",
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
                    subtitle = "Wunschliste öffnen",
                    // Avatar mit Foto oder Initialen
                    // Die Initialen (bis zu 2 Buchstaben) berechnet initialsOf() in ui/components.
                    leading = { InitialsAvatar(name = participant.name, photoFile = participantPhotoFiles[participant.id]) },
                    // Klick auf die ganze Karte öffnet die Wunschliste
                    onClick = { onOpenWishlist(participant.id) },
                    trailing = {
                        // Drei-Punkte-Aktionsmenü (⋮)
                        ItemMenu(
                            contentDescription = "Aktionen für ${participant.name}",
                            actions = listOf(
                                MenuAction("Teilnehmer bearbeiten", Icons.Filled.Edit, { onEditParticipantClick(participant.id) }),
                                MenuAction("Teilnehmer löschen", Icons.Filled.Delete, { onDeleteParticipantClick(participant.id) }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }

        // 7. Historie-Abschnitt (gelost wird nur über "Losen" in der Aktionsleiste)
        item { SectionHeader(title = "Historie") }
        item {
            // Auslosungshistorie Karte
            ListItemCard(
                title = "Auslosungshistorie",
                leading = { IconAvatar(icon = Icons.Filled.History) },
                trailing = { ChevronIcon() },
                onClick = onOpenHistory,
            )
        }
        item {
            // Hinweisfeld
            NoteCard(text = "Alte Ziehungen bleiben, wie sie waren.", modifier = Modifier.padding(top = Spacing.m))
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

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
    PreviewScreen(GroupDetailUiState(isLoading = false, errorMessage = "Fehler beim Laden der Wichtelgruppe aus der Datenbank."))
}

/**
 * Preview 7: Screen mit geöffnetem Teilnehmer-Editor-Dialog.
 */
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

/**
 * Preview 8: Screen mit geöffnetem Teilnehmer-Löschdialog.
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
 * Preview 6: Inhalt mit 12 Teilnehmern.
 */
@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun GroupDetailScreenContentManyParticipantsPreview() {
    val participants = (1..12).map { Participant(id = "p_$it", groupId = "g1", name = "Teilnehmer $it") }
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = participants))
}
