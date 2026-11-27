package com.example.wichtly.ui.wishlist.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.display.InitialsAvatar
import com.example.wichtly.ui.components.display.ListItemCard
import com.example.wichtly.ui.components.display.NumberAvatar
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Stateless screen of a participant's wishlist.
 *
 * One [LazyColumn] scrolls the page header, the participant card, the numbered wishes (edit and
 * delete in the ⋮ menu of each card) and the empty state. "Add wish" sits fixed in the bottom action bar.
 *
 * @param onEditClick edits an existing wish (receives the itemId).
 * @param onDeleteClick deletes a wish (receives the itemId).
 * @param onEditorTextChange the text in the editor dialog changed.
 * @param onSaveClick saves in the editor dialog.
 * @param onDismissEditor cancels or closes the editor dialog.
 * @param onConfirmDelete confirms the delete in the delete dialog.
 * @param onDismissDelete cancels or closes the delete dialog.
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
    val participantName = state.participantName
    val ready = !state.isLoading && state.errorMessage == null && !state.notFound && participantName != null
    // The modifier is applied only here, to the top-level WichtlyScaffold.
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = stringResource(R.string.wishlist_title), onBack = onBack) },
        bottomBar = {
            if (ready) {
                // "Add wish" button across the full width of the bottom action bar
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
            // 1. Loading
            state.isLoading -> LoadingState(text = stringResource(R.string.wishlist_loading))

            // 2. Loading error
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )

            // 3. Participant not found
            state.notFound -> MessageState(
                title = stringResource(R.string.wishlist_participant_missing_title),
                text = stringResource(R.string.wishlist_participant_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )

            participantName == null -> Unit

            // 4. Ready (loaded successfully, whether or not there are wishes)
            else -> WishlistContent(
                participantName = participantName,
                items = state.items,
                isSaving = state.isSaving,
                innerPadding = innerPadding,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
            )
        }
    }

    // Dialogs as an overlay above the content
    if (state.showEditor) {
        WishEditorDialog(
            isEditMode = state.editingItemId != null,
            text = state.editorText,
            errorMessage = state.editorError?.let { stringResource(it, WishlistValidator.MAX_TEXT_LENGTH) },
            isBusy = state.isSaving,
            onTextChange = onEditorTextChange,
            onSave = onSaveClick,
            onDismiss = onDismissEditor,
        )
    }

    val pendingDeleteItem = state.pendingDeleteItemId?.let { pendingId -> state.items.find { it.id == pendingId } }
    if (pendingDeleteItem != null) {
        WishDeleteDialog(
            itemText = pendingDeleteItem.text,
            errorMessage = state.deleteError?.let { stringResource(it) },
            isBusy = state.isSaving,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

/**
 * Main content of the wishlist in the ready state.
 *
 * Uses one [LazyColumn], so all sections scroll together.
 */
@Composable
private fun WishlistContent(
    participantName: String,
    items: List<WishlistItem>,
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
            // (1) Page header with intro text
            PageHeader(
                eyebrow = stringResource(R.string.wishlist_eyebrow),
                title = stringResource(R.string.wishlist_header),
                subtitle = stringResource(R.string.wishlist_subtitle),
            )
        }
        item {
            // (2) Participant card with avatar (initials)
            ListItemCard(
                title = participantName,
                leading = { InitialsAvatar(name = participantName, size = 56.dp) },
            )
        }
        // (3) Section row; the number of wishes is in the gold badge next to the title
        item { SectionHeader(title = stringResource(R.string.wishlist_section), count = items.size) }
        // (4) & (5) Wish entries, or the empty-list hint
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
                    // Wish text (wraps cleanly over up to 4 lines for long texts)
                    title = item.text,
                    titleMaxLines = 4,
                    leading = { NumberAvatar(number = index + 1) },
                    trailing = {
                        // Actions in the ⋮ menu: edit wish and delete wish
                        ItemMenu(
                            enabled = !isSaving,
                            contentDescription = stringResource(R.string.wishlist_item_actions, index + 1),
                            actions = listOf(
                                MenuAction(stringResource(R.string.wishlist_edit), Icons.Filled.Edit, { onEditClick(item.id) }),
                                MenuAction(stringResource(R.string.wishlist_delete), Icons.Filled.Delete, {
                                    onDeleteClick(item.id)
                                }, destructive = true),
                            ),
                        )
                    },
                )
            }
        }
    }
}

private val sampleItems = listOf(
    WishlistItem(id = "1", participantId = "p1", text = "Ein gutes Notizbuch", sortOrder = 0),
    WishlistItem(id = "2", participantId = "p1", text = "Tee aus einer kleinen Manufaktur", sortOrder = 1),
    WishlistItem(id = "3", participantId = "p1", text = "Kaffeemaschine (Espresso)", sortOrder = 2),
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
    PreviewScreen(WishlistUiState(isLoading = false, notFound = true))
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
 * Preview: a very long wish text (shows the line wrapping).
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenLongTextPreview() {
    PreviewScreen(
        WishlistUiState(
            isLoading = false,
            participantName = "Mara",
            items = listOf(
                WishlistItem(
                    id = "1",
                    participantId = "p1",
                    text = "Sehr langer Wunschtext über mehrere Zeilen: Ein handgebundenes Skizzenbuch im Format A4 " +
                        "mit festem Einband und einem dunkelgrünen Lesezeichenband.",
                ),
            ),
        ),
    )
}

/**
 * Preview: screen with the editor dialog open in add mode.
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenWithEditorPreview() {
    PreviewScreen(
        WishlistUiState(
            isLoading = false,
            participantName = "Mara",
            items = sampleItems,
            showEditor = true,
            editorText = "Warme Wollsocken",
        ),
    )
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ParticipantWishlistScreenWithDeleteDialogPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems, pendingDeleteItemId = "2"))
}

/**
 * Preview: exactly 1 wish (the counter in the section header shows 1).
 */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ParticipantWishlistScreenSingleItemPreview() {
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = sampleItems.take(1)))
}

/**
 * Preview: 12 wishes (shows that all elements scroll together).
 */
@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ParticipantWishlistScreenTwelveItemsPreview() {
    val items = (1..12).map {
        WishlistItem(id = "$it", participantId = "p1", text = "Wunschidee Nummer $it zum Verschenken", sortOrder = it)
    }
    PreviewScreen(WishlistUiState(isLoading = false, participantName = "Mara", items = items))
}
