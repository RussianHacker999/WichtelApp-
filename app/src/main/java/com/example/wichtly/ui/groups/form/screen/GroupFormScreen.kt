package com.example.wichtly.ui.groups.form.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.controls.SecondaryButton
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar

/**
 * Stateless screen of the group form (creating and editing a gift group).
 *
 * Built like every screen on [com.example.wichtly.ui.components.layout.WichtlyScaffold]: top bar with
 * a back arrow, page header, photo field (same photo flow as for the participant photo), form fields
 * with a label above and helper texts, selection cards for the draw type, and cancel and save buttons
 * side by side in the bottom action bar.
 *
 * Content: [GroupFormContent]; previews: GroupFormScreenPreviews.kt.
 *
 * @param actions all user actions of the screen, see [GroupFormActions].
 */
@Composable
fun GroupFormScreen(state: GroupFormUiState, actions: GroupFormActions, modifier: Modifier = Modifier) {
    val showForm = !state.isLoading && !state.notFound && !state.loadFailed
    // The modifier is applied only to the top-level WichtlyScaffold.
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            WichtlyTopBar(
                title = stringResource(if (state.isEditMode) R.string.common_edit_group else R.string.action_create_group),
                onBack = actions.onBack,
            )
        },
        bottomBar = {
            if (showForm) {
                // Form actions (cancel and save side by side), only while the form is shown
                BottomActionBar(modifier = Modifier.imePadding()) {
                    SecondaryButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = actions.onBack,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(
                            when {
                                state.isSaving -> R.string.group_form_saving
                                state.isEditMode -> R.string.common_save
                                else -> R.string.group_form_create
                            },
                        ),
                        onClick = actions.onSaveClick,
                        loading = state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            // 1. Loading (edit mode)
            state.isLoading -> Box(Modifier.fillMaxSize()) { LoadingState(text = stringResource(R.string.group_form_loading)) }

            // 2. Group not found
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )

            // 3. Loading error in edit mode: replaces the form, so no empty fields of an existing group appear
            state.loadFailed -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.generalError ?: R.string.error_group_load),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )

            // 4. Form
            else -> GroupFormContent(
                state = state,
                innerPaddingTop = innerPadding,
                onNameChange = actions.onNameChange,
                onBudgetChange = actions.onBudgetChange,
                onDrawDayChange = actions.onDrawDayChange,
                onDrawTypeChange = actions.onDrawTypeChange,
                onPhotoCaptured = actions.onPhotoCaptured,
                onRemovePhoto = actions.onRemovePhoto,
            )
        }
    }
}
