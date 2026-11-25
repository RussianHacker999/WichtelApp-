package com.example.wichtly.ui.groups.form.screen

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import com.example.wichtly.ui.components.display.labelRes
import com.example.wichtly.ui.components.display.descriptionRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.controls.ChoiceCard
import com.example.wichtly.ui.components.controls.FieldLabel
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.controls.SecondaryButton
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.components.layout.WichtlyTopBar
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
import com.example.wichtly.ui.components.photo.PhotoArea
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoPickerField
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import com.example.wichtly.domain.validation.GiftGroupValidator

/**
 * Zustandsloser Composable-Screen für das Gruppen-Formular (Erstellen und Bearbeiten einer Wichtelgruppe).
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [GroupFormUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Aufbau wie alle Screens über [com.example.wichtly.ui.components.layout.WichtlyScaffold]:
 *   - Top-Leiste mit Zurück-Pfeil, darunter der Seitenkopf mit Eyebrow und Titel.
 *   - Fotofeld mit demselben Foto-Ablauf wie beim Teilnehmerfoto.
 *   - Formularfelder mit Label darüber und Hinweistexten (supportingText).
 *   - Auswahlkarten (RadioButton) für die Wichtelart mit kurzer Erklärung.
 *   - Abbrechen- und Speichern-Buttons nebeneinander in der Aktionsleiste unten.
 *
 * @param state Der aktuelle UI-Zustand des Formulars.
 * @param onNameChange Callback bei Änderung des Gruppennamens.
 * @param onBudgetChange Callback bei Änderung des Budgets.
 * @param onDrawDayChange Callback bei Änderung des Auslosungsdatums.
 * @param onDrawTypeChange Callback bei Änderung des Wichtel-Typs.
 * @param onSaveClick Callback zum Auslösen des Speichervorgangs.
 * @param onBack Callback für die Zurück-Navigation.
 * @param modifier Äußerer Modifier für den Screen.
 * @param onPhotoSelectClick Öffnet den Foto-Ablauf.
 * @param onPhotoFlowClose Schließt den Foto-Ablauf.
 * @param onPhotoCaptured Neues Foto aufgenommen.
 * @param onRemovePhoto Foto entfernen.
 * @param onCameraDenied Kamerazugriff abgelehnt.
 * @param onOpenAppSettings Öffnet die App-Einstellungen.
 */
@Composable
fun GroupFormScreen(
    state: GroupFormUiState,
    onNameChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onDrawDayChange: (String) -> Unit,
    onDrawTypeChange: (DrawType) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPhotoSelectClick: () -> Unit = {},
    onPhotoFlowClose: () -> Unit = {},
    onPhotoCaptured: (File) -> Unit = {},
    onRemovePhoto: () -> Unit = {},
    onCameraDenied: () -> Unit = {},
    onOpenAppSettings: (() -> Unit)? = null,
) {
    val showForm = !state.isLoading && !state.notFound
    // Der übergebene modifier wird ausschließlich am Top-Level WichtlyScaffold angewendet.
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            WichtlyTopBar(
                title = stringResource(if (state.isEditMode) R.string.common_edit_group else R.string.action_create_group),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (showForm) {
                // 6. Formular-Aktionen (Abbrechen & Speichern nebeneinander), nur sichtbar, wenn das Formular angezeigt wird
                BottomActionBar(modifier = Modifier.imePadding()) {
                    SecondaryButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = onBack,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(
                            when {
                                state.isSaving -> R.string.group_form_saving
                                state.isEditMode -> R.string.common_save
                                else -> R.string.group_form_create
                            }
                        ),
                        onClick = onSaveClick,
                        loading = state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            // 1. ZUSTAND: Laden (im Bearbeiten-Modus)
            // UI-Darstellung für den Ladezustand (gemeinsamer LoadingState).
            state.isLoading -> Box(Modifier.fillMaxSize()) { LoadingState(text = stringResource(R.string.group_form_loading)) }
            // 2. ZUSTAND: Gruppe nicht gefunden
            // UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde (gemeinsamer MessageState).
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )
            // 3. ZUSTAND: Formular anzeigen
            else -> GroupFormContent(
                state = state,
                innerPaddingTop = innerPadding,
                onNameChange = onNameChange,
                onBudgetChange = onBudgetChange,
                onDrawDayChange = onDrawDayChange,
                onDrawTypeChange = onDrawTypeChange,
                onPhotoSelectClick = onPhotoSelectClick,
                onOpenAppSettings = onOpenAppSettings,
            )
        }
    }

    PhotoCaptureFlow(
        area = PhotoArea.GROUP,
        hasPhoto = state.photoPath != null,
        isOpen = state.isPhotoFlowOpen,
        onClose = onPhotoFlowClose,
        onPhotoCaptured = onPhotoCaptured,
        onRemovePhoto = onRemovePhoto,
        onCameraDenied = onCameraDenied,
    )
}

/**
 * UI-Darstellung des scrollbaren Formulars.
 * Die Aktionsleiste darunter weicht über imePadding() automatisch der Software-Tastatur aus.
 */
@Composable
private fun GroupFormContent(
    state: GroupFormUiState,
    innerPaddingTop: androidx.compose.foundation.layout.PaddingValues,
    onNameChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onDrawDayChange: (String) -> Unit,
    onDrawTypeChange: (DrawType) -> Unit,
    onPhotoSelectClick: () -> Unit,
    onOpenAppSettings: (() -> Unit)?,
) {
    val enabled = !state.isSaving
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(innerPaddingTop),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        item {
            // Seitenkopf mit Eyebrow und Titel (PageHeader)
            PageHeader(
                eyebrow = stringResource(if (state.isEditMode) R.string.group_form_eyebrow_edit else R.string.group_form_eyebrow_create),
                title = stringResource(if (state.isEditMode) R.string.group_form_header_edit else R.string.group_form_header_create),
            )
        }
        item {
            PhotoPickerField(
                area = PhotoArea.GROUP,
                hasPhoto = state.photoPath != null,
                photoFile = state.photoFile,
                onSelectClick = onPhotoSelectClick,
                enabled = enabled,
            )
        }
        if (state.cameraDenied) {
            item { CameraPermissionDeniedHint(onOpenSettings = onOpenAppSettings) }
        }
        state.photoError?.let { error ->
            item { ErrorText(stringResource(error)) }
        }
        item {
            WichtlyTextField(
                // 1. Gruppennamen-Eingabe
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
                // 2. Budget-Eingabe in Euro (Dezimaltastatur)
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
                // 3. Wichteltag-Eingabe mit Format-Hinweis TT.MM.JJJJ
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
                // 4. Auswahl der Wichtel-Art (Auswahlkarten mit RadioButton)
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
        // 5. Allgemeiner Fehlertext
        state.generalError?.let { error ->
            item { ErrorText(stringResource(error)) }
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Erstellen-Modus mit leeren Feldern.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupFormScreenCreatePreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(isEditMode = false, isLoading = false),
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {},
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
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {},
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
                cameraDenied = true,
            ),
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {}, onOpenAppSettings = {},
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
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {},
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
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {},
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
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {},
        )
    }
}
