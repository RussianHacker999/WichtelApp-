package com.example.wichtly.ui.wishlist

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Composable-Screen für die Wunschliste eines Teilnehmers.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md & HTML-Mockup 04):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [WishlistUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Der Parameter [modifier] wird streng NUR am äußersten Element ([Scaffold]) angewendet.
 * - Visuelles Design orientiert sich exakt an Konzept Screen 04 aus dem HTML-Mockup:
 *   - Titelleiste mit Zeichen "‹" und Barrierefreiheits-Semantik.
 *   - Genau eine [LazyColumn] im Bereit-Zustand für gemeinsames Scrollen aller Elemente.
 *   - Teilnehmer-Karte mit Avatar (Initialen) und Wünsche-Anzahl.
 *   - Einleitungstext ("WAS WÜNSCHST DU DIR?", "Kleine Ideen, große Freude.").
 *   - Nummernfelder (1, 2, ...) neben jedem Wunsch und Aktionsicons (✎, ×).
 *   - Leerer Zustand zeigt eine OutlinedCard als Platzhalter innerhalb der Liste.
 *   - Hinweisfeld und "+ Wunsch hinzufügen"-Button am Ende der Liste.
 *
 * @param state Der aktuelle UI-Zustand des Bildschirms.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onAddClick Callback zum Hinzufügen eines neuen Wunsches.
 * @param onEditClick Callback zum Bearbeiten eines bestehenden Wunsches (erhält itemId).
 * @param onDeleteClick Callback zum Löschen eines Wunsches (erhält itemId).
 * @param onEditorTextChange Callback bei Textänderungen im Editor-Dialog.
 * @param onSaveClick Callback zum Speichern im Editor-Dialog.
 * @param onDismissEditor Callback zum Abbrechen/Schließen des Editor-Dialogs.
 * @param onConfirmDelete Callback zum Bestätigen der Löschung im Löschdialog.
 * @param onDismissDelete Callback zum Abbrechen/Schließen des Löschdialogs.
 * @param modifier Äußerer Modifier für den Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantWishlistScreen(
    state: WishlistUiState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (itemId: String) -> Unit,
    onDeleteClick: (itemId: String) -> Unit,
    onEditorTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissEditor: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Der übergebene modifier wird ausschließlich hier am Top-Level Scaffold angewendet.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wunschliste",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Zurück" }
                    ) {
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                    WishlistLoadingState()
                }

                // 2. ZUSTAND: Fehler aufgetreten
                state.errorMessage != null -> {
                    WishlistErrorState(
                        errorMessage = state.errorMessage,
                        onBack = onBack
                    )
                }

                // 3. ZUSTAND: Teilnehmer nicht gefunden (isLoading == false && participantName == null)
                state.participantName == null -> {
                    WishlistNotFoundState(
                        onBack = onBack
                    )
                }

                // 4. ZUSTAND: Bereit (Erfolgreich geladen, unabhängig davon ob Wünsche existieren)
                else -> {
                    WishlistReadyContent(
                        state = state,
                        onAddClick = onAddClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }

            // Dialoge als Overlay über dem Inhalt
            if (state.showEditor) {
                WishlistItemDialog(
                    isEditMode = state.editingItemId != null,
                    text = state.editorText,
                    errorMessage = state.editorError,
                    isSaving = state.isSaving,
                    onTextChange = onEditorTextChange,
                    onSave = onSaveClick,
                    onDismiss = onDismissEditor
                )
            }

            val pendingDeleteItem = state.pendingDeleteItemId?.let { pendingId ->
                state.items.find { it.id == pendingId }
            }
            if (pendingDeleteItem != null) {
                WishlistDeleteDialog(
                    itemText = pendingDeleteItem.text,
                    errorMessage = state.deleteError,
                    isSaving = state.isSaving,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDelete
                )
            }
        }
    }
}

/**
 * Hauptinhalt der Wunschliste im "Bereit"-Zustand.
 * Verwendet genau eine zentrale [LazyColumn], sodass alle Abschnitte gemeinsam scrollen.
 */
@Composable
private fun WishlistReadyContent(
    state: WishlistUiState,
    onAddClick: () -> Unit,
    onEditClick: (itemId: String) -> Unit,
    onDeleteClick: (itemId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // (1) Teilnehmer-Karte mit Avatar (Initialen) und Badge
        item(key = "participant-card") {
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getFirstLetter(state.participantName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.participantName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Persönliche Wunschliste",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = formatWishesCount(state.items.size),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // (2) Einleitungstext
        item(key = "introduction") {
            Column {
                Text(
                    text = "WAS WÜNSCHST DU DIR?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kleine Ideen,\ngroße Freude.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trage ein, was beim Schenken helfen könnte. Die Liste bleibt privat in der Admin-App.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (3) Abschnittszeile
        item(key = "section-header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wünsche",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatEntriesCount(state.items.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (4) & (5) Wunscheinträge ODER Leere-Liste-Hinweis
        if (state.items.isEmpty()) {
            item(key = "empty-hint") {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Noch keine Wünsche eingetragen. Ein paar Ideen helfen beim Schenken, die Liste darf aber auch leer bleiben.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            itemsIndexed(
                items = state.items,
                key = { _, item -> item.id }
            ) { index, item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nummernfeld
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Wunschtext & Untertitel (bricht bei langen Texten sauber um)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Wunsch ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Aktions-Buttons: Bearbeiten (✎) & Löschen (×)
                        IconButton(
                            onClick = { onEditClick(item.id) },
                            modifier = Modifier.semantics { contentDescription = "Wunsch bearbeiten" }
                        ) {
                            Text(
                                text = "✎",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick(item.id) },
                            modifier = Modifier.semantics { contentDescription = "Wunsch löschen" }
                        ) {
                            Text(
                                text = "×",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        // (6) Button über volle Breite am Ende der Liste
        item(key = "add-wish-button") {
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "＋ Wunsch hinzufügen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // (7) Hinweisfeld am Seitenende
        item(key = "hint-box") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Nur einfache Texte in dieser Version: keine Kategorien, Links, Bilder oder Prioritäten. Die Reihenfolge deiner Einträge bleibt erhalten.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * UI-Darstellung für den Ladezustand.
 */
@Composable
private fun WishlistLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Wunschliste wird geladen...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * UI-Darstellung, wenn der Teilnehmer nicht gefunden wurde.
 */
@Composable
private fun WishlistNotFoundState(
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
            text = "Teilnehmer nicht gefunden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Der angeforderte Teilnehmer existiert nicht oder wurde entfernt.",
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
 * UI-Darstellung für den Fehlerfall.
 */
@Composable
private fun WishlistErrorState(
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

// -----------------------------------------------------------------------------
// HILFSFUNKTIONEN (Einzahl / Mehrzahl & Initialen)
// -----------------------------------------------------------------------------

/**
 * Formatiert die Wünsche-Anzahl für das Badge in Einzahl ("1 Wunsch") oder Mehrzahl ("N Wünsche").
 */
private fun formatWishesCount(count: Int): String {
    return if (count == 1) "1 Wunsch" else "$count Wünsche"
}

/**
 * Formatiert die Eintrags-Anzahl für die Abschnittszeile in Einzahl ("1 Eintrag") oder Mehrzahl ("N Einträge").
 */
private fun formatEntriesCount(count: Int): String {
    return if (count == 1) "1 Eintrag" else "$count Einträge"
}

/**
 * Extrahiert den ersten Buchstabe des Teilnehmers in Großschreibung für das Avatar-Feld.
 */
private fun getFirstLetter(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val trimmed = name.trim()
    return if (trimmed.isEmpty()) "?" else trimmed.take(1).uppercase()
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenLoadingPreview() {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = true
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenNotFoundPreview() {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = null
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenErrorPreview() {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Alex",
                errorMessage = "Verbindung zur Datenbank fehlgeschlagen."
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenEmptyPreview() {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Alex",
                items = emptyList()
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview mit genau 1 Wunsch (Einzahl "1 Wunsch", "1 Eintrag").
 */
@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenSingleItemPreview() {
    val sampleItems = listOf(
        WishListItem(
            id = "1",
            participantId = "p1",
            text = "Ein gutes Notizbuch",
            sortOrder = 1
        )
    )

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Mara",
                items = sampleItems
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenWithItemsPreview() {
    val sampleItems = listOf(
        WishListItem(
            id = "1",
            participantId = "p1",
            text = "Ein gutes Notizbuch",
            sortOrder = 1
        ),
        WishListItem(
            id = "2",
            participantId = "p1",
            text = "Tee aus einer kleinen Manufaktur",
            sortOrder = 2
        )
    )

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Mara",
                items = sampleItems
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview mit 12 Wünschen (demonstriert gemeinsames Scrollen aller Elemente).
 */
@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenTwelveItemsPreview() {
    val sampleItems = (1..12).map { index ->
        WishListItem(
            id = "item_$index",
            participantId = "p1",
            text = "Wunschidee Nummer $index zum Verschenken",
            sortOrder = index
        )
    }

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Mara",
                items = sampleItems
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview mit einem sehr langen Wunschtext (demonstriert sauberes Zeilenumbruch-Verhalten).
 */
@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenLongTextPreview() {
    val sampleItems = listOf(
        WishListItem(
            id = "1",
            participantId = "p1",
            text = "Sehr langer Wunschtext über mehrere Zeilen: Ein handgebundenes Skizzenbuch im Format A4 mit festem Einband, cremefarbenem Papier (mindestens 120 g/m²) und einem dunkelgrünen Lesezeichenband.",
            sortOrder = 1
        )
    )

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Mara",
                items = sampleItems
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

/**
 * Preview für den Screen mit geöffnetem Editor-Dialog im Hinzufügen-Modus.
 */
@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenWithEditorPreview() {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Alex",
                showEditor = true,
                editingItemId = null,
                editorText = "Neues Buch",
                isSaving = false
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenWithDeleteDialogPreview() {
    val sampleItems = listOf(
        WishListItem(
            id = "1",
            participantId = "p1",
            text = "Kaffeemaschine (Espresso)",
            sortOrder = 1
        )
    )

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Alex",
                items = sampleItems,
                pendingDeleteItemId = "1",
                isSaving = false
            ),
            onBack = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onEditorTextChange = {},
            onSaveClick = {},
            onDismissEditor = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}
