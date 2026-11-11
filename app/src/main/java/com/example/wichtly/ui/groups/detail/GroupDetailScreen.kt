package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.ui.groups.form.formatCentsForInput
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Zustandsloser Composable-Screen für die Gruppen-Detailansicht (Gruppenstammdaten und Teilnehmerliste).
 *
 * Architektur-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [GroupDetailUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Der Parameter [modifier] wird streng NUR am äußersten Element ([Scaffold]) angewendet.
 * - Textbuttons werden anstelle externer Icons verwendet, um Zusatz-Dependencies zu vermeiden.
 * - Verwendet keinesfalls `!!` für Nullability-Handling.
 * - Sämtliche Inhalte (Stammdaten-Karte und Teilnehmer) befinden sich in genau einer zentralen [LazyColumn].
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
 * @param modifier Äußerer Modifier für den Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    modifier: Modifier = Modifier
) {
    // Der übergebene modifier wird ausschließlich hier am Top-Level Scaffold angewendet.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleText = state.group?.name ?: "Gruppendetails"
                    Text(text = titleText)
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 1. ZUSTAND: Laden
                state.isLoading -> {
                    GroupDetailLoadingState()
                }

                // 2. ZUSTAND: Fehler aufgetreten
                state.errorMessage != null -> {
                    GroupDetailErrorState(
                        errorMessage = state.errorMessage,
                        onBack = onBack
                    )
                }

                // 3. ZUSTAND: Gruppe nicht gefunden
                state.notFound -> {
                    GroupDetailNotFoundState(
                        onBack = onBack
                    )
                }

                // 4. ZUSTAND: Inhalt
                else -> {
                    val group = state.group
                    if (group == null) {
                        // Sichere Handhabung ohne !!: Wenn group null ist, wird die Nicht-gefunden-Ansicht gezeigt.
                        GroupDetailNotFoundState(onBack = onBack)
                    } else {
                        GroupDetailContentState(
                            group = group,
                            participants = state.participants,
                            onEditGroupClick = onEditGroupClick,
                            onOpenWishlist = onOpenWishlist,
                            onAddParticipantClick = onAddParticipantClick,
                            onEditParticipantClick = onEditParticipantClick,
                            onDeleteParticipantClick = onDeleteParticipantClick
                        )
                    }
                }
            }

            // Dialoge liegen als Overlay über dem Inhalt (außerhalb der when-Verzweigung).
            // 1. Participant Editor Dialog (Hinzufügen / Bearbeiten)
            if (state.showParticipantEditor) {
                ParticipantEditorDialog(
                    isEditMode = state.editingParticipantId != null,
                    name = state.participantNameInput,
                    errorMessage = state.participantNameError,
                    isSaving = state.isSaving,
                    onNameChange = onParticipantNameChange,
                    onSave = onSaveParticipant,
                    onDismiss = onDismissParticipantEditor
                )
            }

            // 2. Participant Delete Dialog
            // Zeige den Löschdialog nur, wenn pendingDeleteParticipantId gesetzt ist UND ein Teilnehmer mit dieser ID existiert.
            val pendingDeleteParticipant = state.pendingDeleteParticipantId?.let { pendingId ->
                state.participants.find { it.id == pendingId }
            }
            if (pendingDeleteParticipant != null) {
                ParticipantDeleteDialog(
                    participantName = pendingDeleteParticipant.name,
                    errorMessage = state.participantDeleteError,
                    isSaving = state.isSaving,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDelete
                )
            }
        }
    }
}

/**
 * UI-Darstellung für den Ladezustand.
 */
@Composable
private fun GroupDetailLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gruppendetails werden geladen...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde.
 */
@Composable
private fun GroupDetailNotFoundState(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gruppe nicht gefunden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Zurück zur Übersicht")
        }
    }
}

/**
 * UI-Darstellung im Fehlerfall (z. B. Netzwerk- oder Datenbankfehler).
 */
@Composable
private fun GroupDetailErrorState(
    errorMessage: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Fehler beim Laden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Zurück")
        }
    }
}

/**
 * UI-Darstellung für den Inhalt der Gruppen-Detailansicht.
 *
 * WICHTIG: Der gesamte Inhalt (Stammdaten-Karte, Teilnehmer-Kopf und Teilnehmerliste)
 * liegt in genau EINER zentralen LazyColumn, sodass der komplette Screen gemeinsam scrollt.
 */
@Composable
private fun GroupDetailContentState(
    group: GiftGroup,
    participants: List<Participant>,
    onEditGroupClick: () -> Unit,
    onOpenWishlist: (participantId: String) -> Unit,
    onAddParticipantClick: () -> Unit,
    onEditParticipantClick: (participantId: String) -> Unit,
    onDeleteParticipantClick: (participantId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Karte mit den Stammdaten der Gruppe
        item(key = "group-card") {
            GroupDataCard(
                group = group,
                onEditGroupClick = onEditGroupClick
            )
        }

        // 2. Kopfbereich des Teilnehmer-Abschnitts
        item(key = "participants-header") {
            ParticipantsHeader(
                participantCount = participants.size,
                onAddParticipantClick = onAddParticipantClick
            )
        }

        // 3. Hinweis bei leerer Liste ODER die einzelnen Teilnehmerkarten
        if (participants.isEmpty()) {
            item(key = "participants-empty") {
                EmptyParticipantsHint(
                    onAddParticipantClick = onAddParticipantClick
                )
            }
        } else {
            items(
                items = participants,
                key = { participant -> participant.id }
            ) { participant ->
                // Modifier.padding(bottom = (-8).dp) verringert den Abstand zwischen den
                // Teilnehmerkarten von 16.dp (gemäß spacedBy) auf 8.dp.
                ParticipantCard(
                    participant = participant,
                    onOpenWishlist = { onOpenWishlist(participant.id) },
                    onEditParticipantClick = { onEditParticipantClick(participant.id) },
                    onDeleteParticipantClick = { onDeleteParticipantClick(participant.id) },
                    modifier = Modifier.padding(bottom = (-8).dp)
                )
            }
        }
    }
}

/**
 * Karte mit den Stammdaten der Wichtelgruppe.
 */
@Composable
private fun GroupDataCard(
    group: GiftGroup,
    onEditGroupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Budget: ${formatCentsForInput(group.budgetCents)} €",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Wichteltag: ${formatDateForInput(group.drawDay)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Wichtelart: ${group.drawType.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onEditGroupClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Gruppe bearbeiten")
            }
        }
    }
}

/**
 * Kopfbereich für den Teilnehmer-Abschnitt mit Anzahl und Hinzufügen-Button.
 */
@Composable
private fun ParticipantsHeader(
    participantCount: Int,
    onAddParticipantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Teilnehmer ($participantCount)",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = onAddParticipantClick) {
            Text("Teilnehmer hinzufügen")
        }
    }
}

/**
 * Hinweisansicht, wenn die Gruppe noch keine Teilnehmer hat.
 */
@Composable
private fun EmptyParticipantsHint(
    onAddParticipantClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Diese Gruppe hat noch keine Teilnehmer.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddParticipantClick) {
            Text("Teilnehmer hinzufügen")
        }
    }
}

/**
 * Einzelne Karte für ein Gruppenmitglied in der Teilnehmerliste.
 */
@Composable
private fun ParticipantCard(
    participant: Participant,
    onOpenWishlist: () -> Unit,
    onEditParticipantClick: () -> Unit,
    onDeleteParticipantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Klickbarer Namensbereich inklusive Unterzeile "Wunschliste öffnen" in Primärfarbe
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenWishlist)
            ) {
                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Wunschliste öffnen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditParticipantClick) {
                    Text("Bearbeiten")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDeleteParticipantClick) {
                    Text(
                        text = "Löschen",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Inhalt mit 3 Teilnehmern (inkl. 2 mit identischem Namen "Alex" aber unterschiedlichen IDs).
 * Warum: Überprüft die korrekte Liste mit gemischten Namen und stellt sicher, dass die UUID als Key Konflikte vermeidet.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenContentWithParticipantsPreview() {
    val sampleGroup = GiftGroup(
        id = "g1",
        name = "Büro Wichteln 2026",
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL
    )

    val sampleParticipants = listOf(
        Participant(id = "p1", groupId = "g1", name = "Alex"),
        Participant(id = "p2", groupId = "g1", name = "Alex"), // Identischer Name, andere ID
        Participant(id = "p3", groupId = "g1", name = "Maria")
    )

    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                group = sampleGroup,
                participants = sampleParticipants
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 2: Inhalt mit leerer Teilnehmerliste.
 * Warum: Überprüft, dass die Stammdaten-Karte oben erhalten bleibt und darunter der Hinweis zur leeren Liste erscheint.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenContentEmptyParticipantsPreview() {
    val sampleGroup = GiftGroup(
        id = "g1",
        name = "Familie Wichteln",
        budgetCents = 5000L,
        drawDay = LocalDate.of(2026, 12, 31),
        drawType = DrawType.JUNK
    )

    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                group = sampleGroup,
                participants = emptyList()
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 3: Ladezustand.
 * Warum: Überprüft den Ladeindikator beim Holen der Gruppendaten.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenLoadingPreview() {
    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = true
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 4: Gruppe nicht gefunden.
 * Warum: Überprüft die Fehlermeldung, wenn die Wichtelgruppe nicht mehr existiert.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenNotFoundPreview() {
    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                notFound = true
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 5: Ladefehler auf Screen-Ebene.
 * Warum: Überprüft die Darstellung von Fehlermeldungen (z. B. Datenbankfehler).
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenErrorPreview() {
    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                errorMessage = "Fehler beim Laden der Wichtelgruppe aus der Datenbank."
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 6: Inhalt mit 12 Teilnehmern.
 * Warum: Demonstriert, dass der gesamte Screen (Stammdaten-Karte, Header und lange Teilnehmerliste)
 * in genau einer LazyColumn gemeinsam scrollt.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenContentManyParticipantsPreview() {
    val sampleGroup = GiftGroup(
        id = "g1",
        name = "Große Firmen-Wichtelgruppe 2026",
        budgetCents = 2000L,
        drawDay = LocalDate.of(2026, 12, 18),
        drawType = DrawType.NORMAL
    )

    val twelveParticipants = (1..12).map { index ->
        Participant(
            id = "p_$index",
            groupId = "g1",
            name = "Teilnehmer $index"
        )
    }

    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                group = sampleGroup,
                participants = twelveParticipants
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 7: Screen mit geöffnetem Teilnehmer-Editor-Dialog.
 * Warum: Überprüft die korrekte Überlagerung des ParticipantEditorDialogs über den Screen-Inhalt.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenWithParticipantEditorPreview() {
    val sampleGroup = GiftGroup(
        id = "g1",
        name = "Büro Wichteln 2026",
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL
    )

    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                group = sampleGroup,
                showParticipantEditor = true,
                participantNameInput = "Alex",
                editingParticipantId = null,
                isSaving = false
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview 8: Screen mit geöffnetem Teilnehmer-Löschdialog.
 * Warum: Überprüft die korrekte Überlagerung des ParticipantDeleteDialogs über den Screen-Inhalt.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenWithParticipantDeleteDialogPreview() {
    val sampleGroup = GiftGroup(
        id = "g1",
        name = "Büro Wichteln 2026",
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL
    )

    val sampleParticipants = listOf(
        Participant(id = "p1", groupId = "g1", name = "Alex")
    )

    WichtlyTheme {
        GroupDetailScreen(
            state = GroupDetailUiState(
                isLoading = false,
                group = sampleGroup,
                participants = sampleParticipants,
                pendingDeleteParticipantId = "p1",
                isSaving = false
            ),
            onBack = {},
            onEditGroupClick = {},
            onOpenWishlist = {},
            onAddParticipantClick = {},
            onEditParticipantClick = {},
            onDeleteParticipantClick = {},
            onParticipantNameChange = {},
            onSaveParticipant = {},
            onDismissParticipantEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}
