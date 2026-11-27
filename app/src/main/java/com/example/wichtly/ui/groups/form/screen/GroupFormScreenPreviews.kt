package com.example.wichtly.ui.groups.form.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.theme.WichtlyTheme

/** Empty actions for all previews of the group form. */
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

/**
 * Preview: create mode with empty fields.
 */
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
 * Preview: edit mode with filled form fields.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenEditPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = false,
                name = "Büro Wichteln 2026",
                budget = "25,00",
                drawDay = "24.12.2026",
                drawType = DrawType.NORMAL,
            ),
            actions = previewActions,
        )
    }
}

/**
 * Preview: form with field errors and a general error text.
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
 * Preview: form while saving (isSaving = true).
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenSavingPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = false,
                isSaving = true,
                name = "Familie Wichteln",
                budget = "50,00",
                drawDay = "31.12.2026",
                drawType = DrawType.JUNK,
            ),
            actions = previewActions,
        )
    }
}

/**
 * Preview: loading state in edit mode.
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
 * Preview: group not found (notFound = true).
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
 * Preview: loading error in edit mode (loadFailed = true).
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
