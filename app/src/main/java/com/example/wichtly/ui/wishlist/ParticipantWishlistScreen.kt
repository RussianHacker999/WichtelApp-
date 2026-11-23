package com.example.wichtly.ui.wishlist

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
import com.example.wichtly.ui.components.NoteCard
import com.example.wichtly.ui.components.NumberAvatar
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Screen der Wunschliste eines Teilnehmers (#5).
 *
 * Jeder Wunsch ist eine nummerierte Karte; Bearbeiten und Löschen liegen im ⋮-Menü der Karte
 * (gleich wie bei Teilnehmern). "Wunsch hinzufügen" sitzt fest in der Aktionsleiste.
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
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = "Wunschliste", onBack = onBack) },
        bottomBar = {
            if (ready) {
                BottomActionBar {
                    PrimaryButton(
                        text = "Wunsch hinzufügen",
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
            state.isLoading -> LoadingState(text = "Wunschliste wird geladen...")
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            state.participantName == null -> MessageState(
                title = "Teilnehmer nicht gefunden",
                text = "Der angeforderte Teilnehmer existiert nicht oder wurde entfernt.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
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

    if (state.showEditor) {
        WishlistItemDialog(
            isEditMode = state.editingItemId != null,
            text = state.editorText,
            errorMessage = state.editorError,
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
            errorMessage = state.deleteError,
            isSaving = state.isSaving,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

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
            PageHeader(
                eyebrow = "Was wünschst du dir?",
                title = "Her mit den Wünschen!",
                subtitle = "Trage ein, was beim Schenken helfen könnte.",
            )
        }
        item {
            ListItemCard(
                title = participantName,
                leading = { InitialsAvatar(name = participantName, size = 56.dp) },
            )
        }
        item { SectionHeader(title = "Wünsche", count = items.size) }
        if (items.isEmpty()) {
            item {
                MessageState(
                    title = "Noch keine Wünsche",
                    text = "Ein paar Ideen helfen beim Schenken, die Liste darf aber auch leer bleiben.",
                    icon = Icons.Filled.Redeem,
                    fillMaxSize = false,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                )
            }
        } else {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                ListItemCard(
                    title = item.text,
                    titleMaxLines = 4,
                    leading = { NumberAvatar(number = index + 1) },
                    trailing = {
                        ItemMenu(
                            enabled = !isSaving,
                            contentDescription = "Aktionen für Wunsch ${index + 1}",
                            actions = listOf(
                                MenuAction("Wunsch bearbeiten", Icons.Filled.Edit, { onEditClick(item.id) }),
                                MenuAction("Wunsch löschen", Icons.Filled.Delete, { onDeleteClick(item.id) }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }
        item {
            NoteCard(
                text = "Nur einfache Texte in dieser Version. Die Reihenfolge deiner Einträge bleibt erhalten.",
                modifier = Modifier.padding(top = Spacing.m),
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

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
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Alex", errorMessage = "Verbindung zur Datenbank fehlgeschlagen."))
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
