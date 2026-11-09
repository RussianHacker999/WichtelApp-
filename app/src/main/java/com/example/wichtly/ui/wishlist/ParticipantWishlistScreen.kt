package com.example.wichtly.ui.wishlist

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
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Composable-Screen für die Wunschliste eines Teilnehmers.
 *
 * Architektur-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [WishlistUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Der Parameter [modifier] wird streng NUR am äußersten Element ([Scaffold]) angewendet.
 * - Textbuttons werden anstelle externer Icons verwendet, um Zusatz-Dependencies zu vermeiden.
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
    modifier: Modifier = Modifier
) {
    // Der übergebene modifier wird ausschließlich hier am Top-Level Scaffold angewendet.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when {
                        state.isLoading -> "Wunschliste lädt..."
                        state.participantName != null -> "Wunschliste: ${state.participantName}"
                        else -> "Teilnehmer"
                    }
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

                // 4. ZUSTAND: Bereit (Erfolgreich geladen)
                else -> {
                    if (state.items.isEmpty()) {
                        // Bereit - Liste ist leer
                        WishlistEmptyState(
                            onAddClick = onAddClick
                        )
                    } else {
                        // Bereit - Liste enthält Einträge
                        WishlistContentState(
                            items = state.items,
                            onAddClick = onAddClick,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }

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

            // Löschdialog nur, wenn pendingDeleteItemId gesetzt ist UND ein Item mit dieser ID existiert.
            val pendingDeleteItem = state.pendingDeleteItemId?.let { pendingId ->
                state.items.find { it.id == pendingId }
            }
            if (pendingDeleteItem != null) {
                WishlistDeleteDialog(
                    itemText = pendingDeleteItem.text,
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

/**
 * UI-Darstellung, wenn der Teilnehmer noch keine Wünsche eingetragen hat.
 * Eine leere Liste ist ein gültiger Zustand und kein Fehler.
 */
@Composable
private fun WishlistEmptyState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Noch keine Wünsche vorhanden",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Füge den ersten Wunsch für die Geschenkidee hinzu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Text("Wunsch hinzufügen")
        }
    }
}

/**
 * UI-Darstellung für die gefüllte Wunschliste.
 */
@Composable
private fun WishlistContentState(
    items: List<TemporaryWishlistItem>,
    onAddClick: () -> Unit,
    onEditClick: (itemId: String) -> Unit,
    onDeleteClick: (itemId: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Aktion zum Hinzufügen über der Liste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onAddClick) {
                Text("Wunsch hinzufügen")
            }
        }

        // Wunschliste
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // WICHTIG: it.id (UUID) wird als eindeutiger Schlüssel verwendet, keinesfalls Namen/Texte!
            items(
                items = items,
                key = { item -> item.id }
            ) { item ->
                WishlistItemCard(
                    item = item,
                    onEditClick = { onEditClick(item.id) },
                    onDeleteClick = { onDeleteClick(item.id) }
                )
            }
        }
    }
}

/**
 * Einzelne Card-Darstellung für einen Wunscheintrag.
 */
@Composable
private fun WishlistItemCard(
    item: TemporaryWishlistItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onEditClick) {
                    Text("Bearbeiten")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDeleteClick) {
                    Text(
                        text = "Löschen",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenWithItemsPreview() {
    val sampleItems = listOf(
        TemporaryWishlistItem(
            id = "1",
            participantId = "p1",
            text = "Kaffeemaschine (Espresso)",
            sortOrder = 1
        ),
        TemporaryWishlistItem(
            id = "2",
            participantId = "p1",
            text = "Buch: Clean Architecture von Robert C. Martin",
            sortOrder = 2
        ),
        TemporaryWishlistItem(
            id = "3",
            participantId = "p1",
            text = "Wollsocken in Blau (Größe 42)",
            sortOrder = 3
        )
    )

    WichtlyTheme {
        ParticipantWishlistScreen(
            state = WishlistUiState(
                isLoading = false,
                participantName = "Alex",
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
 * Warum: Überprüft die korrekte Überlagerung des WishlistItemDialog über den Bildschirm-Inhalt.
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
        TemporaryWishlistItem(
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
