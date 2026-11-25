package com.example.wichtly.ui.groups.form

import com.example.wichtly.R
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel für das Formular zum Erstellen und Bearbeiten einer Wichtelgruppe.
 *
 * Warum MVVM & Clean Architecture?
 * Das ViewModel verwaltet den UI-Zustand ([GroupFormUiState]) entkoppelt von der UI.
 * Es verarbeitet Benutzereingaben, führt UI-Formatprüfungen durch und ruft das [GiftGroupRepository] auf.
 *
 * @param groupId ID der zu bearbeitenden Gruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param repository Das Repository für Datenhaltung und Domain-Validierung.
 * @param photoRepository Speichert, löst auf und löscht das Gruppenfoto.
 */
class GroupFormViewModel(
    private val groupId: String?,
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    // Gemerkte Instanz der geladenen Gruppe im Bearbeiten-Modus, um id und photoPath bei copy() beizubehalten
    private var originalGroup: GiftGroup? = null

    // Foto, das aktuell in der Datenbank steht. Alles andere ist ein noch nicht gespeichertes Foto
    // und wird beim Verwerfen oder Ersetzen wieder gelöscht (gleiches Muster wie beim Teilnehmerfoto).
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = false,
                            photoPath = loadedGroup.photoPath,
                            photoFile = photoRepository.getFileForRelativePath(loadedGroup.photoPath),
                            name = loadedGroup.name,
                            budget = formatCentsForInput(loadedGroup.budgetCents),
                            drawDay = formatDateForInput(loadedGroup.drawDay),
                            drawType = loadedGroup.drawType,
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
                        generalError = R.string.error_group_load,
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
     * Warum: isSaved lebt im ViewModel und überlebt die Rotation. Ohne Zurücksetzen
     * würde die Route nach einer Drehung onSaved ein zweites Mal auslösen.
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
        val currentState = _uiState.value

        // Doppel-Submit-Schutz, synchron vor dem Start der Coroutine
        if (currentState.isSaving) return

        // Bearbeiten ohne geladene Gruppe (Ladefehler): nicht speichern, Grund nennen
        if (groupId != null && originalGroup == null) {
            _uiState.update {
                it.copy(generalError = R.string.error_group_not_loaded_cannot_save)
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
                    // Erstellen: Domain-Modell erzeugt die UUID über den Standard-Parameter
                    GiftGroup(
                        name = currentState.name.trim(),
                        budgetCents = parsedBudgetCents,
                        drawDay = parsedDate,
                        drawType = currentState.drawType,
                        photoPath = currentState.photoPath,
                    )
                } else {
                    // Bearbeiten: id bleibt durch copy() erhalten, photoPath kommt aus dem Formular (Foto neu, geändert oder entfernt)
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
                        // Ein ersetztes oder entferntes altes Foto erst nach erfolgreichem Speichern löschen
                        val oldCommitted = committedPhotoPath
                        if (oldCommitted != null && oldCommitted != currentState.photoPath) {
                            deletePhotoQuietly(oldCommitted)
                        }
                        committedPhotoPath = currentState.photoPath
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
                        generalError = R.string.error_group_save,
                    )
                }
            }
        }
    }

    // =========================================================================
    // FOTO (gleicher Ablauf wie beim Teilnehmerfoto)
    // =========================================================================

    /** Öffnet den Foto-Ablauf. */
    fun onPhotoSelectClick() {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isPhotoFlowOpen = true, cameraDenied = false) }
    }

    /** Schließt den Foto-Ablauf. */
    fun onPhotoFlowClose() {
        _uiState.update { it.copy(isPhotoFlowOpen = false) }
    }

    /** Kamerazugriff wurde abgelehnt: Ablauf schließen und Hinweis zeigen. */
    fun onCameraDenied() {
        _uiState.update { it.copy(cameraDenied = true, isPhotoFlowOpen = false) }
    }

    /**
     * Übernimmt ein neu aufgenommenes Foto ins Formular. Es wird sofort in den App-Speicher kopiert,
     * aber erst beim Speichern der Gruppe in der Datenbank eingetragen.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            try {
                val previousPath = _uiState.value.photoPath
                val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.GROUPS)
                if (previousPath != null && previousPath != committedPhotoPath) {
                    deletePhotoQuietly(previousPath)
                }
                _uiState.update {
                    it.copy(
                        photoPath = relativePath,
                        photoFile = photoRepository.getFileForRelativePath(relativePath),
                        photoError = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupFormViewModel", "Fehler beim Speichern des Gruppenfotos", e)
                _uiState.update { it.copy(photoError = R.string.error_photo_save) }
            } finally {
                try {
                    if (tempFile.exists()) tempFile.delete()
                } catch (e: Exception) {
                    Log.e("GroupFormViewModel", "Fehler beim Löschen der temporären Fotodatei", e)
                }
            }
        }
    }

    /** Entfernt das Foto aus dem Formular. Ein gespeichertes Foto wird erst beim Speichern gelöscht. */
    fun onPhotoRemove() {
        val currentPath = _uiState.value.photoPath
        if (currentPath != null && currentPath != committedPhotoPath) {
            viewModelScope.launch { deletePhotoQuietly(currentPath) }
        }
        _uiState.update { it.copy(photoPath = null, photoFile = null, photoError = null) }
    }

    /**
     * Formular wird ohne Speichern verlassen: ein neu aufgenommenes, noch nicht gespeichertes Foto löschen.
     */
    fun onDiscard() {
        val currentPath = _uiState.value.photoPath
        if (currentPath != null && currentPath != committedPhotoPath) {
            viewModelScope.launch { deletePhotoQuietly(currentPath) }
        }
    }

    private suspend fun deletePhotoQuietly(relativePath: String) {
        withContext(NonCancellable) {
            try {
                photoRepository.deletePhoto(relativePath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupFormViewModel", "Fehler beim Löschen eines Gruppenfotos", e)
            }
        }
    }
}
