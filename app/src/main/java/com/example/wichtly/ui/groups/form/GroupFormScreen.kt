package com.example.wichtly.ui.groups.form

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
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.ChoiceCard
import com.example.wichtly.ui.components.FieldLabel
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SecondaryButton
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTextField
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
import com.example.wichtly.ui.components.photo.PhotoArea
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoPickerField
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/**
 * Zustandsloser Screen zum Erstellen und Bearbeiten einer Wichtelgruppe (#2).
 *
 * Aufbau wie jeder Screen: Top-Leiste, Seitenkopf, Felder (Foto, Name, Budget, Wichteltag,
 * Wichtelart) und unten die Aktionsleiste mit Abbrechen / Speichern.
 * Das Gruppenfoto läuft über denselben Foto-Ablauf wie das Teilnehmerfoto.
 *
 * @param state Der aktuelle UI-Zustand des Formulars.
 * @param onNameChange Änderung des Gruppennamens.
 * @param onBudgetChange Änderung des Budgets.
 * @param onDrawDayChange Änderung des Wichteltags.
 * @param onDrawTypeChange Änderung der Wichtelart.
 * @param onSaveClick Speichern.
 * @param onBack Zurück bzw. Abbrechen.
 * @param modifier Äußerer Modifier.
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
    WichtlyScaffold(
        modifier = modifier,
        topBar = {
            WichtlyTopBar(
                title = if (state.isEditMode) "Gruppe bearbeiten" else "Neue Gruppe anlegen",
                onBack = onBack,
            )
        },
        bottomBar = {
            if (showForm) {
                BottomActionBar(modifier = Modifier.imePadding()) {
                    SecondaryButton(
                        text = "Abbrechen",
                        onClick = onBack,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = when {
                            state.isSaving -> "Wird gespeichert..."
                            state.isEditMode -> "Speichern"
                            else -> "Gruppe erstellen"
                        },
                        onClick = onSaveClick,
                        loading = state.isSaving,
                        modifier = Modifier.weight(1.4f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize()) { LoadingState(text = "Wichtelgruppe wird geladen...") }
            state.notFound -> MessageState(
                title = "Diese Gruppe ist verschwunden",
                text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
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
            PageHeader(
                eyebrow = if (state.isEditMode) "Gruppenstammdaten" else "Neue Wichtelgruppe",
                title = if (state.isEditMode) "Gruppe aktualisieren." else "Losgewichtelt!",
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
            item { ErrorText(error) }
        }
        item {
            WichtlyTextField(
                label = "Gruppenname *",
                value = state.name,
                onValueChange = onNameChange,
                placeholder = "Familie Wichteln",
                errorText = state.nameError,
                enabled = enabled,
            )
        }
        item {
            WichtlyTextField(
                label = "Geschenkbudget (€) *",
                value = state.budget,
                onValueChange = onBudgetChange,
                placeholder = "25,00",
                supportingText = "Wie viel darf ein Geschenk kosten?",
                errorText = state.budgetError,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
        item {
            WichtlyTextField(
                label = "Wichteltag *",
                value = state.drawDay,
                onValueChange = onDrawDayChange,
                placeholder = "24.12.2026",
                supportingText = "Datum im Format TT.MM.JJJJ (z. B. 24.12.2026).",
                errorText = state.drawDayError,
                enabled = enabled,
                trailingIcon = { Icon(imageVector = Icons.Filled.Event, contentDescription = null) },
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                FieldLabel(text = "Wichtelart *")
                DrawType.entries.forEach { type ->
                    ChoiceCard(
                        title = type.displayName,
                        description = drawTypeDescription(type),
                        selected = state.drawType == type,
                        onClick = { onDrawTypeChange(type) },
                        enabled = enabled,
                    )
                }
            }
        }
        state.generalError?.let { error ->
            item { ErrorText(error) }
        }
    }
}

private fun drawTypeDescription(type: DrawType): String = when (type) {
    DrawType.NORMAL -> "Jeder schenkt einer Person."
    DrawType.JUNK -> "Kurioses statt Nützliches."
}

@Composable
private fun ErrorText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

// =============================================================================
// PREVIEWS
// =============================================================================

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

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupFormScreenErrorsPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = false, isLoading = false, name = "", budget = "abc", drawDay = "31.02.2026",
                nameError = "Der Gruppenname darf nicht leer sein.",
                budgetError = "Ungültiges Budget. Bitte gib einen Betrag wie 25,50 ein.",
                drawDayError = "Ungültiges Datum. Bitte verwende das Format TT.MM.JJJJ (z. B. 24.12.2026).",
                generalError = "Ein unerwarteter Fehler ist aufgetreten.",
                cameraDenied = true,
            ),
            onNameChange = {}, onBudgetChange = {}, onDrawDayChange = {}, onDrawTypeChange = {},
            onSaveClick = {}, onBack = {}, onOpenAppSettings = {},
        )
    }
}

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
