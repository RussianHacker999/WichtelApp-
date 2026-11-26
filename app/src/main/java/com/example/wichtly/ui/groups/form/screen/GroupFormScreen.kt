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
 * @param actions Alle Nutzeraktionen des Screens, siehe [GroupFormActions].
 * @param modifier Äußerer Modifier für den Screen.
 *
 * Inhalt: [GroupFormContent]; Previews: GroupFormScreenPreviews.kt.
 */
@Composable
fun GroupFormScreen(
    state: GroupFormUiState,
    actions: GroupFormActions,
    modifier: Modifier = Modifier,
) {
    val showForm = !state.isLoading && !state.notFound && !state.loadFailed
    // Der übergebene modifier wird ausschließlich am Top-Level WichtlyScaffold angewendet.
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
                // 6. Formular-Aktionen (Abbrechen & Speichern nebeneinander), nur sichtbar, wenn das Formular angezeigt wird
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
                            }
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
                onAction = actions.onBack,
            )
            // 3. ZUSTAND: Ladefehler im Bearbeiten-Modus
            // Ersetzt das Formular, damit keine leeren Felder einer bestehenden Gruppe erscheinen (gemeinsamer MessageState).
            state.loadFailed -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.generalError ?: R.string.error_group_load),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = actions.onBack,
            )
            // 4. ZUSTAND: Formular anzeigen
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
