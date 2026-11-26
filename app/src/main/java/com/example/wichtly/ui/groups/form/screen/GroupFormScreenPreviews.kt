package com.example.wichtly.ui.groups.form.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.theme.WichtlyTheme

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Erstellen-Modus mit leeren Feldern.
 */

private val previewActions = GroupFormActions(
    onNameChange = { _ -> },
    onBudgetChange = { _ -> },
    onDrawDayChange = { _ -> },
    onDrawTypeChange = { _ -> },
    onSaveClick = { },
    onBack = { },
    onPhotoCaptured = { },
    onRemovePhoto = { },
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenCreatePreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(isEditMode = false, isLoading = false),
            actions = previewActions,
        )
    }
}

/**
 * Preview 2: Bearbeiten-Modus mit gefüllten Formularfeldern.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenEditPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true, isLoading = false, name = "Büro Wichteln 2026",
                budget = "25,00", drawDay = "24.12.2026", drawType = DrawType.NORMAL,
            ),
            actions = previewActions,
        )
    }
}

/**
 * Preview 3: Formular mit aktiven Feldfehlern und allgemeinem Fehlertext.
 */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupFormScreenErrorsPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = false, isLoading = false, name = "", budget = "abc", drawDay = "31.02.2026",
                nameError = R.string.error_group_name_empty,
                budgetError = R.string.error_budget_format,
                drawDayError = R.string.error_date_format,
                generalError = R.string.error_group_save,
                photo = PhotoFieldUiState(error = R.string.error_photo_save),
            ),
            actions = previewActions,
        )
    }
}

/**
 * Preview 4: Formular während des laufenden Speichervorgangs (isSaving = true).
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenSavingPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true, isLoading = false, isSaving = true, name = "Familie Wichteln",
                budget = "50,00", drawDay = "31.12.2026", drawType = DrawType.JUNK,
            ),
            actions = previewActions,
        )
    }
}

/**
 * Preview 5: Ladezustand im Bearbeiten-Modus.
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenLoadingPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(isEditMode = true, isLoading = true),
            actions = previewActions,
        )
    }
}

/**
 * Preview 6: Gruppe nicht gefunden (notFound = true).
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenNotFoundPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(isEditMode = true, isLoading = false, notFound = true),
            actions = previewActions,
        )
    }
}

/**
 * Preview 7: Ladefehler im Bearbeiten-Modus (loadFailed = true).
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenLoadFailedPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = false,
                loadFailed = true,
                generalError = R.string.error_group_load,
            ),
            actions = previewActions,
        )
    }
}
