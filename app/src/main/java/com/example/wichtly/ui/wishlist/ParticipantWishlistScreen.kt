package com.example.wichtly.ui.wishlist

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.InitialsAvatar
import com.example.wichtly.ui.components.ItemMenu
import com.example.wichtly.ui.components.ListItemCard
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MenuAction
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.NumberAvatar
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.domain.validation.WishListValidator

/**
 * Zustandsloser Composable-Screen für die Wunschliste eines Teilnehmers.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [WishlistUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Der Parameter [modifier] wird streng NUR am äußersten Element
 *   ([com.example.wichtly.ui.components.WichtlyScaffold]) angewendet.
 * - Aufbau wie alle Screens:
 *   - Top-Leiste mit Zurück-Pfeil (Icon mit contentDescription "Zurück").
 *   - Genau eine [LazyColumn] im Bereit-Zustand für gemeinsames Scrollen aller Elemente.
 *   - Seitenkopf ("Was wünschst du dir?", "Her mit den Wünschen!") und darunter die Teilnehmer-Karte
 *     mit Avatar (Initialen).
 *   - Nummernfelder (1, 2, ...) neben jedem Wunsch; Bearbeiten und Löschen im ⋮-Menü der Karte.
 *   - Leerer Zustand zeigt einen Hinweis (MessageState) innerhalb der Liste.
 *   - Hinweisfeld am Ende der Liste, "Wunsch hinzufügen" fest in der Aktionsleiste unten.
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
    val ready = !state.isLoading && state.errorMessage == null && state.participantName != null
    // Der übergebene modifier wird ausschließlich hier am Top-Level WichtlyScaffold angewendet.
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = stringResource(R.string.wishlist_title), onBack = onBack) },
        bottomBar = {
            if (ready) {
                // (6) Button "Wunsch hinzufügen" über volle Breite in der Aktionsleiste unten
                BottomActionBar {
                    PrimaryButton(
                        text = stringResource(R.string.wishlist_add),
                        onClick = onAddClick,
                        icon = Icons.Filled.Add,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            // 1. ZUSTAND: Laden
            // UI-Darstellung für den Ladezustand (gemeinsamer LoadingState).
            state.isLoading -> LoadingState(text = stringResource(R.string.wishlist_loading))
            // 2. ZUSTAND: Fehler aufgetreten
            // UI-Darstellung für den Fehlerfall (gemeinsamer MessageState).
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )
            // 3. ZUSTAND: Teilnehmer nicht gefunden (isLoading == false && participantName == null)
            // UI-Darstellung, wenn der Teilnehmer nicht gefunden wurde (gemeinsamer MessageState).
            state.participantName == null -> MessageState(
                title = stringResource(R.string.wishlist_participant_missing_title),
                text = stringResource(R.string.wishlist_participant_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )
            // 4. ZUSTAND: Bereit (Erfolgreich geladen, unabhängig davon ob Wünsche existieren)
            else -> WishlistContent(
                participantName = state.participantName,
                items = state.items,
                isSaving = state.isSaving,
                innerPadding = innerPadding,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
            )
        }
    }

    // Dialoge als Overlay über dem Inhalt
    if (state.showEditor) {
        WishlistItemDialog(
            isEditMode = state.editingItemId != null,
            text = state.editorText,
            errorMessage = state.editorError?.let { stringResource(it, WishListValidator.MAX_TEXT_LENGTH) },
            isSaving = state.isSaving,
            onTextChange = onEditorTextChange,
            onSave = onSaveClick,
            onDismiss = onDismissEditor,
        )
    }

    val pendingDeleteItem = state.pendingDeleteItemId?.let { pendingId -> state.items.find { it.id == pendingId } }
    if (pendingDeleteItem != null) {
        WishlistDeleteDialog(
            itemText = pendingDeleteItem.text,
            errorMessage = state.deleteError?.let { stringResource(it) },
            isSaving = state.isSaving,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

/**
 * Hauptinhalt der Wunschliste im "Bereit"-Zustand.
 * Verwendet genau eine zentrale [LazyColumn], sodass alle Abschnitte gemeinsam scrollen.
 */
@Composable
private fun WishlistContent(
    participantName: String,
    items: List<WishListItem>,
    isSaving: Boolean,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onEditClick: (itemId: String) -> Unit,
    onDeleteClick: (itemId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
    ) {
        item {
            // (1) Seitenkopf mit Einleitungstext
            PageHeader(
                eyebrow = stringResource(R.string.wishlist_eyebrow),
                title = stringResource(R.string.wishlist_header),
                subtitle = stringResource(R.string.wishlist_subtitle),
            )
        }
        item {
            // (2) Teilnehmer-Karte mit Avatar (Initialen)
            // Die Initialen berechnet initialsOf() in ui/components.
            ListItemCard(
                title = participantName,
                leading = { InitialsAvatar(name = participantName, size = 56.dp) },
            )
        }
        // (3) Abschnittszeile
        // Die Anzahl der Wünsche steht als Zahl im goldenen Badge neben dem Titel.
        item { SectionHeader(title = stringResource(R.string.wishlist_section), count = items.size) }
        // (4) & (5) Wunscheinträge ODER Leere-Liste-Hinweis
        if (items.isEmpty()) {
            item {
                MessageState(
                    title = stringResource(R.string.wishlist_empty_title),
                    text = stringResource(R.string.wishlist_empty_text),
                    icon = Icons.Filled.Redeem,
                    fillMaxSize = false,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                )
            }
        } else {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                ListItemCard(
                    // Wunschtext (bricht bei langen Texten nach bis zu 4 Zeilen sauber um)
                    title = item.text,
                    titleMaxLines = 4,
                    // Nummernfeld
                    leading = { NumberAvatar(number = index + 1) },
                    trailing = {
                        // Aktionen im ⋮-Menü: Wunsch bearbeiten & Wunsch löschen
                        ItemMenu(
                            enabled = !isSaving,
                            contentDescription = stringResource(R.string.wishlist_item_actions, index + 1),
                            actions = listOf(
                                MenuAction(stringResource(R.string.wishlist_edit), Icons.Filled.Edit, { onEditClick(item.id) }),
                                MenuAction(stringResource(R.string.wishlist_delete), Icons.Filled.Delete, { onDeleteClick(item.id) }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }
    }
}


// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

private val sampleItems = listOf(
    WishListItem(id = "1", participantId = "p1", text = "Ein gutes Notizbuch", sortOrder = 0),
    WishListItem(id = "2", participantId = "p1", text = "Tee aus einer kleinen Manufaktur", sortOrder = 1),
    WishListItem(id = "3", participantId = "p1", text = "Kaffeemaschine (Espresso)", sortOrder = 2),
)

@Composable
private fun PreviewScreen(state: WishlistUiState) {
    WichtlyTheme {
        ParticipantWishlistScreen(
            state = state,
            onBack = {}, onAddClick = {}, onEditClick = {}, onDeleteClick = {},
            onEditorTextChange = {}, onSaveClick = {}, onDismissEditor = {},
            onConfirmDelete = {}, onDismissDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenLoadingPreview() {
    PreviewScreen(WishlistUiState(isLoading = true))
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenNotFoundPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = null))
}

@Preview(showBackground = true)
@Composable
private fun ParticipantWishlistScreenErrorPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Alex", errorMessage = R.string.error_wishlist_load))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ParticipantWishlistScreenEmptyPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Alex", items = emptyList()))
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenWithItemsPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems))
}

/**
 * Preview mit einem sehr langen Wunschtext (demonstriert sauberes Zeilenumbruch-Verhalten).
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenLongTextPreview() {
    PreviewScreen(
        WishlistUiState(
            isLoading = false,
            participantName = "Mara",
            items = listOf(
                WishListItem(
                    id = "1",
                    participantId = "p1",
                    text = "Sehr langer Wunschtext über mehrere Zeilen: Ein handgebundenes Skizzenbuch im Format A4 mit festem Einband und einem dunkelgrünen Lesezeichenband.",
                ),
            ),
        )
    )
}

/**
 * Preview für den Screen mit geöffnetem Editor-Dialog im Hinzufügen-Modus.
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenWithEditorPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems, showEditor = true, editorText = "Warme Wollsocken"))
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenWithDeleteDialogPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems, pendingDeleteItemId = "2"))
}

/**
 * Preview mit genau 1 Wunsch (Einzahl "1 Wunsch", "1 Eintrag").
 */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ParticipantWishlistScreenSingleItemPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems.take(1)))
}

/**
 * Preview mit 12 Wünschen (demonstriert gemeinsames Scrollen aller Elemente).
 */
@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ParticipantWishlistScreenTwelveItemsPreview() {
    val items = (1..12).map { WishListItem(id = "$it", participantId = "p1", text = "Wunschidee Nummer $it zum Verschenken", sortOrder = it) }
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = items))
}
