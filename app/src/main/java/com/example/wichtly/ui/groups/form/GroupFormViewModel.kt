package com.example.wichtly.ui.groups.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel für das Formular zum Erstellen und Bearbeiten einer Wichtelgruppe.
 *
 * Warum MVVM & Clean Architecture?
 * Das ViewModel verwaltet den UI-Zustand ([GroupFormUiState]) entkoppelt von der UI.
 * Es verarbeitet Benutzereingaben, führt UI-Formatprüfungen durch und ruft das [GiftGroupRepository] auf.
 *
 * @param groupId ID der zu bearbeitenden Gruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param repository Das Repository für Datenhaltung und Domain-Validierung.
 */
class GroupFormViewModel(
    private val groupId: String?,
    private val repository: GiftGroupRepository
) : ViewModel() {

    // Gemerkte Instanz der geladenen Gruppe im Bearbeiten-Modus, um id und photoPath bei copy() beizubehalten
    private var originalGroup: GiftGroup? = null

    private val _uiState = MutableStateFlow(
        GroupFormUiState(
            isEditMode = groupId != null,
            isLoading = groupId != null
        )
    )
    val uiState: StateFlow<GroupFormUiState> = _uiState.asStateFlow()

    init {
        // Im Bearbeiten-Modus wird die bestehende Gruppe genau einmal geladen
        if (groupId != null) {
            loadGroup(groupId)
        }
    }

    /**
     * Lädt die Wichtelgruppe zum Bearbeiten genau einmal mit repository.getGiftGroupById(id).first().
     */
    private fun loadGroup(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                // Einmaliges Auslesen des ersten Flow-Werts (keine dauerhafte Beobachtung)
                val loadedGroup = repository.getGiftGroupById(id).first()
                if (loadedGroup == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = true
                        )
                    }
                } else {
                    originalGroup = loadedGroup
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = false,
                            name = loadedGroup.name,
                            budget = formatCentsForInput(loadedGroup.budgetCents),
                            drawDay = formatDateForInput(loadedGroup.drawDay),
                            drawType = loadedGroup.drawType
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Fehler beim Laden der Wichtelgruppe."
                    )
                }
            }
        }
    }

    // =========================================================================
    // EINGABE-CALLBACKS
    // =========================================================================

    /**
     * Setzt den neuen Gruppennamen und löscht den Fehler des Namensfeldes.
     */
    fun onNameChange(newName: String) {
        _uiState.update {
            it.copy(
                name = newName,
                nameError = null
            )
        }
    }

    /**
     * Setzt das neue Budget und löscht den Fehler des Budgetfeldes.
     */
    fun onBudgetChange(newBudget: String) {
        _uiState.update {
            it.copy(
                budget = newBudget,
                budgetError = null
            )
        }
    }

    /**
     * Setzt das neue Auslosungsdatum und löscht den Fehler des Datumsfeldes.
     */
    fun onDrawDayChange(newDrawDay: String) {
        _uiState.update {
            it.copy(
                drawDay = newDrawDay,
                drawDayError = null
            )
        }
    }

    /**
     * Setzt den ausgewählten Wichtel-Typ.
     */
    fun onDrawTypeChange(newDrawType: DrawType) {
        _uiState.update {
            it.copy(drawType = newDrawType)
        }
    }

    // =========================================================================
    // SPEICHERN
    // =========================================================================

    /**
     * Löst den Speichervorgang aus.
     *
     * Ablauf:
     * 1. Synchroner Doppel-Submit-Schutz vor dem Starten der Coroutine.
     * 2. Formatprüfung von Budget und Datum vor dem Repository-Aufruf. Bei Fehlern werden
     *    die Konstanten [INVALID_BUDGET_FORMAT_ERROR] bzw. [INVALID_DATE_FORMAT_ERROR] gesetzt und abgebrochen.
     * 3. Erstellen der [GiftGroup] (Neu: leere ID, photoPath null; Bearbeiten: copy() der geladenen Gruppe).
     * 4. Aufruf von createGiftGroup bzw. updateGiftGroup im Repository.
     * 5. Verarbeiten von Success (isSaved = true), ValidationError (Fehlerübersetzung) und Exceptions.
     */
    fun onSaveClick() {
        // Synchroner Doppel-Submit-Schutz vor Coroutine-Start
        if (_uiState.value.isSaving) return

        val currentState = _uiState.value

        // Formatprüfungen
        val parsedBudgetCents = parseBudgetToCents(currentState.budget)
        val parsedDate = parseDate(currentState.drawDay)

        var hasFormatError = false
        var budgetErr = currentState.budgetError
        var dateErr = currentState.drawDayError

        if (parsedBudgetCents == null) {
            budgetErr = INVALID_BUDGET_FORMAT_ERROR
            hasFormatError = true
        }

        if (parsedDate == null) {
            dateErr = INVALID_DATE_FORMAT_ERROR
            hasFormatError = true
        }

        if (hasFormatError) {
            _uiState.update {
                it.copy(
                    budgetError = budgetErr,
                    drawDayError = dateErr
                )
            }
            return
        }

        // Formatierung ist gültig -> Speichern im Repository ausführen
        _uiState.update {
            it.copy(
                isSaving = true,
                nameError = null,
                budgetError = null,
                drawDayError = null,
                generalError = null
            )
        }

        viewModelScope.launch {
            try {
                val groupToSave = if (groupId == null) {
                    // Erstellen: Leere ID (Repository erzeugt UUID), photoPath ist null. Name getrimmt.
                    GiftGroup(
                        id = "",
                        name = currentState.name.trim(),
                        budgetCents = parsedBudgetCents!!,
                        drawDay = parsedDate!!,
                        drawType = currentState.drawType,
                        photoPath = null
                    )
                } else {
                    // Bearbeiten: copy() der geladenen Gruppe, damit photoPath und id erhalten bleiben.
                    originalGroup!!.copy(
                        name = currentState.name.trim(),
                        budgetCents = parsedBudgetCents!!,
                        drawDay = parsedDate!!,
                        drawType = currentState.drawType
                    )
                }

                val result = if (groupId == null) {
                    repository.createGiftGroup(groupToSave)
                } else {
                    repository.updateGiftGroup(groupToSave)
                }

                when (result) {
                    is GiftGroupResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isSaved = true
                            )
                        }
                    }
                    is GiftGroupResult.ValidationError -> {
                        _uiState.update { state ->
                            applyDomainValidationErrors(state, result.errors).copy(isSaving = false)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        generalError = "Fehler beim Speichern der Wichtelgruppe."
                    )
                }
            }
        }
    }
}
