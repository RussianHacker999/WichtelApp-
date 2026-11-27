package com.example.wichtly.ui.groups.form.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.ui.components.controls.ChoiceCard
import com.example.wichtly.ui.components.controls.FieldLabel
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.components.display.descriptionRes
import com.example.wichtly.ui.components.display.labelRes
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.photo.screen.PhotoField
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/**
 * Scrollable content of the form.
 *
 * The action bar below it moves out of the way of the software keyboard via imePadding().
 */
@Composable
internal fun GroupFormContent(
    state: GroupFormUiState,
    innerPaddingTop: PaddingValues,
    onNameChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onDrawDayChange: (String) -> Unit,
    onDrawTypeChange: (DrawType) -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
) {
    val enabled = !state.isSaving
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPaddingTop),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        item {
            // Page header with eyebrow and title
            PageHeader(
                eyebrow = stringResource(if (state.isEditMode) R.string.group_form_eyebrow_edit else R.string.group_form_eyebrow_create),
                title = stringResource(if (state.isEditMode) R.string.group_form_header_edit else R.string.group_form_header_create),
            )
        }
        item {
            PhotoField(
                photo = state.photo,
                onPhotoCaptured = onPhotoCaptured,
                onRemovePhoto = onRemovePhoto,
                enabled = enabled,
            )
        }
        item {
            WichtlyTextField(
                // 1. Group name
                label = stringResource(R.string.group_form_name_label),
                value = state.name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.group_form_name_placeholder),
                errorText = state.nameError?.let { stringResource(it, GiftGroupValidator.MAX_NAME_LENGTH) },
                enabled = enabled,
                maxLength = GiftGroupValidator.MAX_NAME_LENGTH,
            )
        }
        item {
            WichtlyTextField(
                // 2. Budget in euros (decimal keyboard)
                label = stringResource(R.string.group_form_budget_label),
                value = state.budget,
                onValueChange = onBudgetChange,
                placeholder = stringResource(R.string.group_form_budget_placeholder),
                supportingText = stringResource(R.string.group_form_budget_support),
                errorText = state.budgetError?.let { stringResource(it, GiftGroupValidator.BUDGET_LIMIT_EUROS) },
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
        item {
            WichtlyTextField(
                // 3. Draw day with the format hint DD.MM.YYYY
                label = stringResource(R.string.group_form_date_label),
                value = state.drawDay,
                onValueChange = onDrawDayChange,
                placeholder = stringResource(R.string.group_form_date_placeholder),
                supportingText = stringResource(
                    if (state.drawDayPassed) R.string.group_form_date_passed_support else R.string.group_form_date_support,
                ),
                errorText = state.drawDayError?.let { stringResource(it, GiftGroupValidator.MAX_YEARS_AHEAD) },
                enabled = enabled,
                trailingIcon = { Icon(imageVector = Icons.Filled.Event, contentDescription = null) },
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                // 4. Draw type (selection cards with radio button)
                FieldLabel(text = stringResource(R.string.group_form_type_label))
                DrawType.entries.forEach { type ->
                    ChoiceCard(
                        title = stringResource(type.labelRes()),
                        description = stringResource(type.descriptionRes()),
                        selected = state.drawType == type,
                        onClick = { onDrawTypeChange(type) },
                        enabled = enabled,
                    )
                }
            }
        }
        // 5. General error text
        state.generalError?.let { error ->
            item { ErrorText(stringResource(error)) }
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormContentPreview() {
    WichtlyTheme {
        GroupFormContent(
            state = GroupFormUiState(isEditMode = false, isLoading = false, name = "Familie Wichteln"),
            innerPaddingTop = PaddingValues(),
            onNameChange = { _ -> },
            onBudgetChange = { _ -> },
            onDrawDayChange = { _ -> },
            onDrawTypeChange = { _ -> },
            onPhotoCaptured = { },
            onRemovePhoto = { },
        )
    }
}
