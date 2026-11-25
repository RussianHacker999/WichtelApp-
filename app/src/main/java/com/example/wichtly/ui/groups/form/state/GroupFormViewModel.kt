package com.example.wichtly.ui.groups.form.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.domain.usecase.GiftGroupInput
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.components.display.formatCentsForInput
import com.example.wichtly.ui.components.display.formatDateForInput
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

private const val TAG = "GroupFormViewModel"

/**
 * ViewModel für das Formular zum Erstellen und Bearbeiten einer Wichtelgruppe.
 *
 * Warum MVVM & Clean Architecture?
 * Das ViewModel verwaltet den UI-Zustand ([GroupFormUiState]) entkoppelt von der UI.
 * Es nimmt Benutzereingaben entgegen, gibt sie unverändert an den [SaveGiftGroupUseCase] weiter
 * und übersetzt dessen Fehler in Texte. Geprüft wird ausschließlich im Use Case.
 *
 * @param groupId ID der zu bearbeitenden Gruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param repository Liest die zu bearbeitende Gruppe.
 * @param photoRepository Speichert, löst auf und löscht das Gruppenfoto.
 * @param saveGiftGroup Prüft und speichert die Gruppe.
 * @param savedStateHandle Hält die Eingaben, auch wenn Android den Prozess im Hintergrund beendet.
 */
class GroupFormViewModel(
    private val groupId: String?,
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
    private val saveGiftGroup: SaveGiftGroupUseCase,
    // liefert das heutige Datum (Wichteltag vorbei?); in Tests ersetzbar
    private val today: () -> LocalDate = { LocalDate.now() },
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Eingaben aus einem früheren Prozess; null, wenn das Formular frisch geöffnet wurde.
    // Wird vor dem Start der Sicherung gelesen, damit sie nicht überschrieben werden.
    private val restoredInputs: FormInputs? = FormInputs.readFrom(savedStateHandle)

    // Geladene Gruppe im Bearbeiten-Modus; null, solange (noch) nichts geladen werden konnte
    private var originalGroup: GiftGroup? = null

    // Foto, das aktuell in der Datenbank steht. Alles andere ist ein noch nicht gespeichertes Foto
    // und wird beim Verwerfen oder Ersetzen wieder gelöscht (gleiches Muster wie beim Teilnehmerfoto).
    private var committedPhotoPath: String? = null

    private val _uiState = MutableStateFlow(
        GroupFormUiState(
            isEditMode = groupId != null,
            isLoading = groupId != null,
        ).let { initial -> if (groupId == null) restoredInputs?.applyTo(initial) ?: initial else initial }
    )
    val uiState: StateFlow<GroupFormUiState> = _uiState.asStateFlow()

    init {
        // Im Bearbeiten-Modus wird die bestehende Gruppe genau einmal geladen
        if (groupId != null) {
            loadGroup(groupId)
        } else if (restoredInputs?.photoPath != null) {
            viewModelScope.launch {
                val file = photoRepository.getFileForRelativePath(restoredInputs.photoPath)
                _uiState.update { it.copy(photoFile = file) }
            }
        }
        // Eingaben laufend sichern, sobald das Formular steht (nicht während des Ladens)
        _uiState
            .filter { !it.isLoading }
            .onEach { FormInputs.of(it).writeTo(savedStateHandle) }
            .launchIn(viewModelScope)
    }

    /**
     * Lädt die Wichtelgruppe zum Bearbeiten genau einmal (repository.getGiftGroupById), ohne sie danach zu beobachten.
     */
    private fun loadGroup(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val loadedGroup = repository.getGiftGroupById(id)
                if (loadedGroup == null) {
                    AppLog.w(TAG, "Gruppe $id zum Bearbeiten nicht gefunden")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = true,
                        )
                    }
                } else {
                    originalGroup = loadedGroup
                    committedPhotoPath = loadedGroup.photoPath
                    val fromDatabase = FormInputs(
                        name = loadedGroup.name,
                        budget = formatCentsForInput(loadedGroup.budgetCents),
                        drawDay = formatDateForInput(loadedGroup.drawDay),
                        drawType = loadedGroup.drawType,
                        photoPath = loadedGroup.photoPath,
                    )
                    // Nach einem Prozesstod gewinnen die zuletzt eingegebenen Werte
                    val inputs = restoredInputs ?: fromDatabase
                    val photoFile = photoRepository.getFileForRelativePath(inputs.photoPath)
                    _uiState.update {
                        inputs.applyTo(it).copy(
                            isLoading = false,
                            notFound = false,
                            photoFile = photoFile,
                            drawDayPassed = !DrawRules.isDrawDayOpen(loadedGroup.drawDay, today()),
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Fehler beim Laden der Wichtelgruppe", e)
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
     * Setzt savedGroupId zurück, nachdem die Route das Signal verarbeitet hat.
     * Warum: savedGroupId lebt im ViewModel und überlebt die Rotation. Ohne Zurücksetzen
     * würde die Route nach einer Drehung onSaved ein zweites Mal auslösen.
     */
    fun onSavedHandled() {
        _uiState.update { it.copy(savedGroupId = null) }
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
     * 2. Rohe Eingaben an den [SaveGiftGroupUseCase] übergeben. Er parst Budget und Datum, prüft alle
     *    Regeln in einem Durchlauf und speichert nur bei fehlerfreiem Ergebnis.
     * 3. Verarbeiten von Success (savedGroupId setzen), ValidationError (Fehlerübersetzung) und Exceptions.
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

        _uiState.update {
            it.copy(
                isSaving = true,
                nameError = null,
                budgetError = null,
                drawDayError = null,
                generalError = null,
            )
        }

        val input = GiftGroupInput(
            name = currentState.name,
            budget = currentState.budget,
            drawDay = currentState.drawDay,
            drawType = currentState.drawType,
            // Foto neu, geändert oder entfernt: der aktuelle Stand des Formulars wird gespeichert
            photoPath = currentState.photoPath,
        )

        viewModelScope.launch {
            try {
                when (val result = saveGiftGroup(groupId, input)) {
                    is GiftGroupResult.Success -> {
                        // Ein ersetztes oder entferntes altes Foto erst nach erfolgreichem Speichern löschen
                        val oldCommitted = committedPhotoPath
                        if (oldCommitted != null && oldCommitted != currentState.photoPath) {
                            photoRepository.deletePhotoQuietly(oldCommitted, TAG)
                        }
                        committedPhotoPath = currentState.photoPath
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                savedGroupId = result.giftGroup.id,
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
                AppLog.e(TAG, "Fehler beim Speichern der Wichtelgruppe", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        generalError = saveErrorMessage(e, R.string.error_group_save),
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

    /** Kamera startet nicht oder die Aufnahme scheitert: Ablauf schließen und Fehler zeigen. */
    fun onPhotoCaptureError() {
        _uiState.update { it.copy(photoError = R.string.error_photo_capture, isPhotoFlowOpen = false) }
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
                    photoRepository.deletePhotoQuietly(previousPath, TAG)
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
                AppLog.e(TAG, "Fehler beim Speichern des Gruppenfotos", e)
                _uiState.update { it.copy(photoError = saveErrorMessage(e, R.string.error_photo_save)) }
            } finally {
                try {
                    if (tempFile.exists()) tempFile.delete()
                } catch (e: Exception) {
                    AppLog.w(TAG, "Fehler beim Löschen der temporären Fotodatei", e)
                }
            }
        }
    }

    /** Entfernt das Foto aus dem Formular. Ein gespeichertes Foto wird erst beim Speichern gelöscht. */
    fun onRemovePhoto() {
        val currentPath = _uiState.value.photoPath
        if (currentPath != null && currentPath != committedPhotoPath) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
        }
        _uiState.update { it.copy(photoPath = null, photoFile = null, photoError = null) }
    }

    /**
     * Formular wird ohne Speichern verlassen: ein neu aufgenommenes, noch nicht gespeichertes Foto löschen.
     */
    fun onDiscard() {
        // Während des Speicherns gehört das Foto womöglich schon zur gespeicherten Gruppe
        if (_uiState.value.isSaving) return
        val currentPath = _uiState.value.photoPath
        if (currentPath != null && currentPath != committedPhotoPath) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
        }
    }

    companion object {
        /**
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
         * @param groupId null = neue Gruppe, sonst die zu bearbeitende Gruppe.
         */
        fun factory(container: AppContainer, groupId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupFormViewModel(
                    groupId = groupId,
                    repository = container.giftGroupRepository,
                    photoRepository = container.photoRepository,
                    saveGiftGroup = container.saveGiftGroupUseCase,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}

/**
 * Die Eingaben des Formulars, wie sie im SavedStateHandle liegen.
 * Fehlertexte und Ladezustand werden nicht gesichert; sie entstehen beim nächsten Speichern neu.
 */
internal data class FormInputs(
    val name: String,
    val budget: String,
    val drawDay: String,
    val drawType: DrawType,
    val photoPath: String?,
) {
    fun applyTo(state: GroupFormUiState): GroupFormUiState = state.copy(
        name = name,
        budget = budget,
        drawDay = drawDay,
        drawType = drawType,
        photoPath = photoPath,
    )

    fun writeTo(handle: SavedStateHandle) {
        handle[KEY_PRESENT] = true
        handle[KEY_NAME] = name
        handle[KEY_BUDGET] = budget
        handle[KEY_DRAW_DAY] = drawDay
        handle[KEY_DRAW_TYPE] = drawType.name
        handle[KEY_PHOTO_PATH] = photoPath
    }

    companion object {
        internal const val KEY_PRESENT = "group_form_inputs"
        internal const val KEY_NAME = "group_form_name"
        internal const val KEY_BUDGET = "group_form_budget"
        internal const val KEY_DRAW_DAY = "group_form_draw_day"
        internal const val KEY_DRAW_TYPE = "group_form_draw_type"
        internal const val KEY_PHOTO_PATH = "group_form_photo_path"

        fun of(state: GroupFormUiState) = FormInputs(state.name, state.budget, state.drawDay, state.drawType, state.photoPath)

        fun readFrom(handle: SavedStateHandle): FormInputs? {
            if (handle.get<Boolean>(KEY_PRESENT) != true) return null
            return FormInputs(
                name = handle[KEY_NAME] ?: "",
                budget = handle[KEY_BUDGET] ?: "",
                drawDay = handle[KEY_DRAW_DAY] ?: "",
                drawType = handle.get<String>(KEY_DRAW_TYPE)
                    ?.let { name -> DrawType.entries.firstOrNull { it.name == name } }
                    ?: DrawType.NORMAL,
                photoPath = handle[KEY_PHOTO_PATH],
            )
        }
    }
}
