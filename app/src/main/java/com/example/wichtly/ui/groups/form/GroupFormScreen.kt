package com.example.wichtly.ui.groups.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Composable-Screen für das Gruppen-Formular (Erstellen und Bearbeiten einer Wichtelgruppe).
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md & HTML-Mockup 02):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [GroupFormUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Visuelles Design orientiert sich exakt an Konzept Screen 02 aus dem HTML-Mockup:
 *   - Hero-Bereich mit Eyebrow, Haupttitel und beschreibendem Untertitel.
 *   - Formularfelder mit detaillierten Hinweistexten (supportingText).
 *   - RadioButtons für Wichtelart mit Erklärungen.
 *   - Abbrechen- und Speichern-Buttons nebeneinander in einer Button-Row.
 *
 * @param state Der aktuelle UI-Zustand des Formulars.
 * @param onNameChange Callback bei Änderung des Gruppennamens.
 * @param onBudgetChange Callback bei Änderung des Budgets.
 * @param onDrawDayChange Callback bei Änderung des Auslosungsdatums.
 * @param onDrawTypeChange Callback bei Änderung des Wichtel-Typs.
 * @param onSaveClick Callback zum Auslösen des Speichervorgangs.
 * @param onBack Callback für die Zurück-Navigation.
 * @param modifier Äußerer Modifier für den Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
) {
    // Der übergebene modifier wird ausschließlich am Top-Level Scaffold angewendet.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.isEditMode) "Gruppe bearbeiten" else "Gruppe erstellen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isEditMode) "Gruppenstammdaten ändern" else "Neue Gruppe anlegen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 1. ZUSTAND: Laden (im Bearbeiten-Modus)
                state.isLoading -> {
                    GroupFormLoadingState()
                }

                // 2. ZUSTAND: Gruppe nicht gefunden
                state.notFound -> {
                    GroupFormNotFoundState(onBack = onBack)
                }

                // 3. ZUSTAND: Formular anzeigen
                else -> {
                    GroupFormContentState(
                        state = state,
                        onNameChange = onNameChange,
                        onBudgetChange = onBudgetChange,
                        onDrawDayChange = onDrawDayChange,
                        onDrawTypeChange = onDrawTypeChange,
                        onSaveClick = onSaveClick,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

/**
 * UI-Darstellung für den Ladezustand.
 */
@Composable
private fun GroupFormLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Wichtelgruppe wird geladen...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde.
 */
@Composable
private fun GroupFormNotFoundState(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gruppe nicht gefunden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Zurück zur Übersicht")
        }
    }
}

/**
 * UI-Darstellung des scrollbaren Formulars.
 * Weicht über imePadding() automatisch der Software-Tastatur aus.
 */
@Composable
private fun GroupFormContentState(
    state: GroupFormUiState,
    onNameChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onDrawDayChange: (String) -> Unit,
    onDrawTypeChange: (DrawType) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero-Header gemäß HTML-Mockup 02
        Column {
            Text(
                text = if (state.isEditMode) "GRUPPENSTAMMDATEN" else "NEUE WICHTELGRUPPE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (state.isEditMode) "Gruppe aktualisieren." else "Deine Runde beginnt.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Name, Budget, Wichteltag und Wichtelart kannst du später in dieser Gruppe erneut bearbeiten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Gruppennamen-Eingabe
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Gruppenname *") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.nameError != null,
            supportingText = {
                Text(
                    text = state.nameError
                        ?: "Der Name muss nicht eindeutig sein. Die Gruppe wird intern über ihre ID identifiziert."
                )
            }
        )

        // 2. Budget-Eingabe in Euro (Dezimaltastatur)
        OutlinedTextField(
            value = state.budget,
            onValueChange = onBudgetChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Geschenkbudget (€) *") },
            singleLine = true,
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.budgetError != null,
            supportingText = {
                Text(
                    text = state.budgetError ?: "Gemeinsames Richtbudget pro Geschenk."
                )
            }
        )

        // 3. Wichteltag-Eingabe mit Format-Hinweis TT.MM.JJJJ
        OutlinedTextField(
            value = state.drawDay,
            onValueChange = onDrawDayChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Wichteltag *") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.drawDayError != null,
            supportingText = {
                Text(
                    text = state.drawDayError ?: "Datum im Format TT.MM.JJJJ (z. B. 24.12.2026)."
                )
            }
        )

        // 4. Auswahl der Wichtel-Art (RadioButtons)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Wichtelart *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            DrawType.entries.forEach { type ->
                val isSelected = state.drawType == type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            enabled = !state.isSaving,
                            role = Role.RadioButton,
                            onClick = { onDrawTypeChange(type) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !state.isSaving
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.isSaving) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Text(
                text = "Die Oberfläche zeigt die gewählte Art. Die JUNK-Regeln werden nicht vorweggenommen; Auslosungen bleiben bis zur fachlichen Festlegung gesperrt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Allgemeiner Fehlertext
        state.generalError?.let { errorText ->
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        // 6. Formular-Aktionen (Abbrechen & Speichern nebeneinander)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Abbrechen")
            }
            Button(
                onClick = onSaveClick,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (state.isSaving) "Wird gespeichert..."
                    else if (state.isEditMode) "Änderungen speichern"
                    else "Gruppe erstellen"
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Erstellen-Modus mit leeren Feldern.
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenCreateEmptyPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = false,
                isLoading = false
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}

/**
 * Preview 2: Bearbeiten-Modus mit gefüllten Formularfeldern.
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenEditFilledPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = false,
                name = "Büro Wichteln 2026",
                budget = "25,00",
                drawDay = "24.12.2026",
                drawType = DrawType.NORMAL
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}

/**
 * Preview 3: Formular mit aktiven Feldfehlern und allgemeinem Fehlertext.
 */
@Preview(showBackground = true)
@Composable
private fun GroupFormScreenWithErrorsPreview() {
    WichtlyTheme {
        GroupFormScreen(
            state = GroupFormUiState(
                isEditMode = false,
                isLoading = false,
                name = "",
                budget = "abc",
                drawDay = "31.02.2026",
                nameError = "Der Gruppenname darf nicht leer sein.",
                budgetError = "Ungültiges Budget. Bitte gib einen Betrag wie 25,50 ein.",
                drawDayError = "Ungültiges Datum. Bitte verwende das Format TT.MM.JJJJ (z. B. 24.12.2026).",
                generalError = "Ein unerwarteter Fehler ist aufgetreten."
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}

/**
 * Preview 4: Formular während des laufenden Speichervorgangs (isSaving = true).
 */
@Preview(showBackground = true)
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
                drawType = DrawType.JUNK
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
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
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = true
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
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
            state = GroupFormUiState(
                isEditMode = true,
                isLoading = false,
                notFound = true
            ),
            onNameChange = {},
            onBudgetChange = {},
            onDrawDayChange = {},
            onDrawTypeChange = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}
