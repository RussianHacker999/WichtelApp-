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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md & HTML-Mockup 03):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [GroupDetailUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Visuelles Design orientiert sich exakt an Konzept Screen 03 aus dem HTML-Mockup:
 *   - Oberer Hero-Bereich mit Eyebrow, Gruppennamen & Untertitel.
 *   - 3 Statistik-Karten für Teilnehmer, Budget und Durchläufe.
 *   - Stammdaten-Karte der Wichtelgruppe.
 *   - Teilnehmerliste mit Avataren (Initialen) und Drei-Punkte-Aktionsmenü (⋮) für Bearbeiten/Löschen.
 *   - Auslosungs- & Historie-Abschnitt.
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
    var groupMenuExpanded by remember { mutableStateOf(false) }

    // Der übergebene modifier wird ausschließlich am Top-Level Scaffold angewendet.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val titleText = state.group?.name ?: "Gruppendetails"
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gruppenbereich",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                },
                actions = {
                    if (state.group != null) {
                        Box {
                            IconButton(onClick = { groupMenuExpanded = true }) {
                                Text(
                                    text = "⋮",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            DropdownMenu(
                                expanded = groupMenuExpanded,
                                onDismissRequest = { groupMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Gruppe bearbeiten") },
                                    onClick = {
                                        groupMenuExpanded = false
                                        onEditGroupClick()
                                    }
                                )
                            }
                        }
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

            // Dialoge als Overlay über dem Inhalt
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
 * UI-Darstellung im Fehlerfall.
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
 * UI-Darstellung für den Inhalt der Gruppen-Detailansicht (gemäß Konzept 03 des HTML-Mockups).
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
        // 1. Eyebrow & Hero Title
        item(key = "group-hero-header") {
            Column {

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

            }
        }

        // 2. Stats Grid (3 Stat-Karten: Teilnehmer, Budget, Durchläufe)
        item(key = "group-stats-row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stat 1: Teilnehmer
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${participants.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Teilnehmer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stat 2: Budget
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${formatCentsForInput(group.budgetCents)} €",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stat 3: Durchläufe
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Durchläufe",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Stammdaten-Karte der Gruppe
        item(key = "group-card") {
            GroupDataCard(
                group = group,
                onEditGroupClick = onEditGroupClick
            )
        }

        // 4. Kopfbereich des Teilnehmer-Abschnitts
        item(key = "participants-header") {
            ParticipantsHeader(
                participantCount = participants.size
            )
        }

        // 5. Hinweis bei leerer Liste ODER Teilnehmerkarten mit 3-Punkte-Menü
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
                ParticipantCard(
                    participant = participant,
                    onOpenWishlist = { onOpenWishlist(participant.id) },
                    onEditParticipantClick = { onEditParticipantClick(participant.id) },
                    onDeleteParticipantClick = { onDeleteParticipantClick(participant.id) }
                )
            }
        }

        // 6. Button: Teilnehmer hinzufügen
        item(key = "add-participant-button") {
            Button(
                onClick = onAddParticipantClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("＋ Teilnehmer hinzufügen")
            }
        }

        // 7. Auslosung & Historie Abschnitt
        item(key = "draw-section") {
            DrawAndHistorySection(
                group = group,
                participantCount = participants.size
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gruppenstammdaten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onEditGroupClick) {
                    Text("Gruppe bearbeiten")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Wichteltag: ${formatDateForInput(group.drawDay)} · Wichtelart: ${group.drawType.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Kopfbereich für den Teilnehmer-Abschnitt mit Anzahl.
 */
@Composable
private fun ParticipantsHeader(
    participantCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Teilnehmer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$participantCount ${if (participantCount == 1) "Teilnehmer" else "Teilnehmer"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Hinweisansicht, wenn die Gruppe noch keine Teilnehmer hat.
 */
@Composable
private fun EmptyParticipantsHint(
    onAddParticipantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "♧",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Diese Gruppe hat noch keine Teilnehmer.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Füge die Personen hinzu, die an der Wichtelrunde teilnehmen. Doppelte Namen sind erlaubt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onAddParticipantClick) {
                Text("Teilnehmer hinzufügen")
            }
        }
    }
}

/**
 * Einzelne Karte für ein Gruppenmitglied in der Teilnehmerliste.
 * Zeigt einen Avatar mit den Namensinitialen sowie das Drei-Punkte-Aktionsmenü (⋮) für Bearbeiten & Löschen.
 */
@Composable
private fun ParticipantCard(
    participant: Participant,
    onOpenWishlist: () -> Unit,
    onEditParticipantClick: () -> Unit,
    onDeleteParticipantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar mit Initialen
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getInitials(participant.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Klickbarer Bereich für Wunschliste
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenWishlist)
            ) {
                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Wunschliste öffnen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Drei-Punkte-Aktionsmenü (⋮)
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Text(
                        text = "⋮",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Teilnehmer bearbeiten") },
                        onClick = {
                            menuExpanded = false
                            onEditParticipantClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Teilnehmer löschen",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteParticipantClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Bereich für "Auslosung & Historie" im Gruppen-Detailbereich.
 */
@Composable
private fun DrawAndHistorySection(
    group: GiftGroup,
    participantCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Auslosung & Historie",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Neue Auslosung Karte
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Neue Auslosung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val statusText = when {
                        participantCount < 2 -> "Teilnehmer fehlen"
                        group.drawType == DrawType.NORMAL -> "Vorbereitung möglich"
                        else -> "Regeln offen"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Die Vorbereitung prüft Teilnehmerliste, Wichtelart und Snapshot-Basis. Erst nach Bestätigung wird ein neuer Durchlauf angelegt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Auslosung vorbereiten */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Auslosung durchführen →")
                }
            }
        }

        // Auslosungshistorie Karte
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "↺",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auslosungshistorie",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Noch keine gespeicherten Durchläufe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { /* Historie */ }) {
                    Text("Ansehen")
                }
            }
        }

        // Hinweisfeld
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = "Historische Ergebnisse bleiben unveränderte Snapshots. Teilnehmernamen und Wünsche werden in der Historie nicht aus den aktuellen Datensätzen rekonstruiert.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Extrahiert bis zu 2 Namensinitialen für die Avatar-Darstellung.
 */
private fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Inhalt mit 3 Teilnehmern (inkl. Initialen und 3-Punkte-Menü).
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
        Participant(id = "p2", groupId = "g1", name = "Alex Smith"),
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
