package com.example.wichtly.ui.groups.form

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel für das Formular zum Erstellen und Bearbeiten einer Wichtelgruppe.
 *
 * Warum MVVM & Clean Architecture?
 * Das ViewModel verwaltet den UI-Zustand ([GroupFormUiState]) entkoppelt von der UI.
 * Es verarbeitet Benutzereingaben, führt UI-Formatprüfungen durch und ruft das [GiftGroupRepository] sowie [PhotoRepository] auf.
 *
 * @param groupId ID der zu bearbeitenden Gruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param repository Das Repository für Datenhaltung und Domain-Validierung.
 * @param photoRepository Das Repository für Foto-Verwaltung und -Speicherung.
 */
class GroupFormViewModel(
    private val groupId: String?,
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    // Gemerkte Instanz der geladenen Gruppe im Bearbeiten-Modus, um id bei copy() beizubehalten
    private var originalGroup: GiftGroup? = null

    // Pfad, der aktuell in der Datenbank gespeichert ist (Erstellen: null; Bearbeiten: loadedGroup.photoPath)
    private var committedPhotoPath: String? = null

    private val _uiState = MutableStateFlow(
        GroupFormUiState(
            isEditMode = groupId != null,
            isLoading = groupId != null,
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
                            notFound = true,
                        )
                    }
                } else {
                    originalGroup = loadedGroup
                    committedPhotoPath = loadedGroup.photoPath
                    val resolvedFile = photoRepository.getFileForRelativePath(loadedGroup.photoPath)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = false,
                            name = loadedGroup.name,
                            budget = formatCentsForInput(loadedGroup.budgetCents),
                            drawDay = formatDateForInput(loadedGroup.drawDay),
                            drawType = loadedGroup.drawType,
                            photoPath = loadedGroup.photoPath,
                            photoFile = resolvedFile,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupFormViewModel", "Fehler beim Laden der Wichtelgruppe", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Fehler beim Laden der Wichtelgruppe.",
                    )
                }
            }
        }
    }

    // =========================================================================
    // EINGABE- & FOTO-CALLBACKS
    // =========================================================================

    /**
     * Setzt den neuen Gruppennamen und löscht den Fehler des Namensfeldes.
     */
    fun onNameChange(newName: String) {
        _uiState.update {
            it.copy(
                name = newName,
                nameError = null,
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
                budgetError = null,
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
                drawDayError = null,
            )
        }
    }

    /**
     * Setzt isSaved zurück, nachdem die Route das Signal verarbeitet hat.
     */
    fun onSavedHandled() {
        _uiState.update { it.copy(isSaved = false) }
    }

    /**
     * Setzt den ausgewählten Wichtel-Typ.
     */
    fun onDrawTypeChange(newDrawType: DrawType) {
        _uiState.update {
            it.copy(drawType = newDrawType)
        }
    }

    /**
     * Öffnet den Foto-Aufnahme-Workflow, sofern kein Speichervorgang läuft.
     */
    fun onPhotoSelectClick() {
        if (_uiState.value.isSaving) return
        _uiState.update {
            it.copy(
                isPhotoFlowOpen = true,
                cameraDenied = false,
            )
        }
    }

    /**
     * Schließt den Foto-Aufnahme-Workflow.
     */
    fun onPhotoFlowClose() {
        _uiState.update {
            it.copy(isPhotoFlowOpen = false)
        }
    }

    /**
     * Markiert, dass der Kamerazugriff abgelehnt wurde.
     */
    fun onCameraDenied() {
        _uiState.update {
            it.copy(cameraDenied = true)
        }
    }

    /**
     * Verarbeitet ein neu aufgenommenes Foto: speichert es im [PhotoRepository],
     * verwirft ein eventuell zuvor temporär aufgenommenes ungespeichertes Foto
     * und aktualisiert den State.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            try {
                val previousPath = _uiState.value.photoPath
                val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.GROUPS)

                // War vorher ein NICHT committetes Foto im State, dieses löschen
                if (previousPath != committedPhotoPath && previousPath != null) {
                    try {
                        photoRepository.deletePhoto(previousPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupFormViewModel", "Fehler beim Löschen des vorherigen ungespeicherten Fotos", e)
                    }
                }

                val resolvedFile = photoRepository.getFileForRelativePath(relativePath)
                _uiState.update {
                    it.copy(
                        photoPath = relativePath,
                        photoFile = resolvedFile,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupFormViewModel", "Fehler beim Speichern des Fotos", e)
                _uiState.update {
                    it.copy(generalError = "Foto konnte nicht gespeichert werden.")
                }
            } finally {
                try {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("GroupFormViewModel", "Fehler beim Löschen der temporären Fotodatei", e)
                }
            }
        }
    }

    /**
     * Entfernt das Foto. Ist es nicht committet, wird es sofort gelöscht.
     * Ist es das committete Gruppenfoto, wird es erst beim Speichern gelöscht.
     */
    fun onPhotoRemove() {
        val currentPath = _uiState.value.photoPath
        if (currentPath != committedPhotoPath && currentPath != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    try {
                        photoRepository.deletePhoto(currentPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupFormViewModel", "Fehler beim Löschen des ungespeicherten Fotos", e)
                    }
                }
            }
        }
        _uiState.update {
            it.copy(
                photoPath = null,
                photoFile = null,
            )
        }
    }

    /**
     * Verwirft nicht gespeicherte Foto-Änderungen (z. B. beim Abbrechen oder Zurückkehren)
     * und setzt den Zustand auf [committedPhotoPath] zurück.
     */
    fun onDiscardPhotoChanges() {
        val currentPath = _uiState.value.photoPath
        val oldCommitted = committedPhotoPath
        if (currentPath != oldCommitted && currentPath != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    try {
                        photoRepository.deletePhoto(currentPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupFormViewModel", "Fehler beim Verwerfen der Foto-Änderungen", e)
                    }
                }
            }
        }
        val resolvedCommittedFile = photoRepository.getFileForRelativePath(oldCommitted)
        _uiState.update {
            it.copy(
                photoPath = oldCommitted,
                photoFile = resolvedCommittedFile,
            )
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
     * 2. Formatprüfung von Budget und Datum vor dem Repository-Aufruf.
     * 3. Erstellen der [GiftGroup] inklusive [photoPath].
     * 4. Aufruf von createGiftGroup bzw. updateGiftGroup im Repository.
     * 5. Bei Erfolg: alten committeten Pfad ggf. endgültig löschen und [committedPhotoPath] aktualisieren.
     */
    fun onSaveClick() {
        val currentState = _uiState.value

        // Doppel-Submit-Schutz, synchron vor dem Start der Coroutine
        if (currentState.isSaving) return

        // Bearbeiten ohne geladene Gruppe (Ladefehler): nicht speichern, Grund nennen
        if (groupId != null && originalGroup == null) {
            _uiState.update {
                it.copy(generalError = "Die Wichtelgruppe konnte nicht geladen werden. Speichern ist nicht möglich.")
            }
            return
        }

        // Formatprüfung vor dem Repository-Aufruf
        val parsedBudgetCents = parseBudgetToCents(currentState.budget)
        val parsedDate = parseDate(currentState.drawDay)
        if (parsedBudgetCents == null || parsedDate == null) {
            _uiState.update {
                it.copy(
                    budgetError = if (parsedBudgetCents == null) INVALID_BUDGET_FORMAT_ERROR else null,
                    drawDayError = if (parsedDate == null) INVALID_DATE_FORMAT_ERROR else null,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                nameError = null,
                budgetError = null,
                drawDayError = null,
                generalError = null,
            )
        }

        val original = originalGroup

        viewModelScope.launch {
            try {
                val groupToSave = if (original == null) {
                    GiftGroup(
                        name = currentState.name.trim(),
                        budgetCents = parsedBudgetCents,
                        drawDay = parsedDate,
                        drawType = currentState.drawType,
                        photoPath = currentState.photoPath,
                    )
                } else {
                    original.copy(
                        name = currentState.name.trim(),
                        budgetCents = parsedBudgetCents,
                        drawDay = parsedDate,
                        drawType = currentState.drawType,
                        photoPath = currentState.photoPath,
                    )
                }

                val result = if (original == null) {
                    repository.createGiftGroup(groupToSave)
                } else {
                    repository.updateGiftGroup(groupToSave)
                }

                when (result) {
                    is GiftGroupResult.Success -> {
                        val oldCommitted = committedPhotoPath
                        val newPath = currentState.photoPath

                        // Wenn sich das committete Foto geändert hat, alten Pfad endgültig löschen
                        if (oldCommitted != null && oldCommitted != newPath) {
                            withContext(NonCancellable) {
                                try {
                                    photoRepository.deletePhoto(oldCommitted)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e("GroupFormViewModel", "Fehler beim Löschen des alten Gruppenfotos", e)
                                }
                            }
                        }

                        committedPhotoPath = newPath

                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isSaved = true,
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
            } catch (e: Exception) {
                Log.e("GroupFormViewModel", "Fehler beim Speichern der Wichtelgruppe", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        generalError = "Fehler beim Speichern der Wichtelgruppe.",
                    )
                }
            }
        }
    }
}
