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
import com.example.wichtly.domain.result.GiftGroupResult
import com.example.wichtly.domain.usecase.GiftGroupInput
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.photo.state.PhotoDraft
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

    // Gruppenfoto als Entwurf: merkt sich das gespeicherte Foto und räumt ungespeicherte Fotos beim
    // Ersetzen, Entfernen oder Verwerfen auf (dieselbe Klasse wie beim Teilnehmerfoto).
    private val photoDraft = PhotoDraft(
        photoRepository = photoRepository,
        ownerType = PhotoOwnerType.GROUPS,
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        committedPathKey = KEY_COMMITTED_PHOTO,
        tag = TAG,
    )

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
                _uiState.update { it.copy(photo = it.photo.copy(file = file)) }
            }
        }
        // Eingaben laufend sichern, sobald das Formular steht (nicht während des Ladens). Nach einem Ladefehler
        // nicht: Die leeren Felder würden sonst nach einem Prozesstod die Werte aus der Datenbank überschreiben.
        _uiState
            .filter { !it.isLoading && !it.loadFailed }
            .onEach { FormInputs.of(it).writeTo(savedStateHandle) }
            .launchIn(viewModelScope)
    }

    /**
     * Lädt die Wichtelgruppe zum Bearbeiten genau einmal (repository.getGiftGroupById), ohne sie danach zu beobachten.
     */
    private fun loadGroup(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false, generalError = null) }
            try {
                val loadedGroup = repository.getGiftGroupById(id)
                if (loadedGroup == null) {
                    AppLog.w(TAG, "Group $id to edit not found")
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    originalGroup = loadedGroup
                    photoDraft.start(committedPath = loadedGroup.photoPath)
                    val fromDatabase = FormInputs.of(loadedGroup)
                    // Nach einem Prozesstod gewinnen die zuletzt eingegebenen Werte
                    val inputs = restoredInputs ?: fromDatabase
                    val photoFile = photoRepository.getFileForRelativePath(inputs.photoPath)
                    _uiState.update {
                        val withInputs = inputs.applyTo(it)
                        withInputs.copy(
                            isLoading = false,
                            notFound = false,
                            photo = withInputs.photo.copy(file = photoFile),
                            drawDayPassed = !DrawRules.isDrawDayOpen(loadedGroup.drawDay, today()),
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to load the group", e)
                _uiState.update { it.copy(isLoading = false, loadFailed = true, generalError = R.string.error_group_load) }
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
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    /**
     * Setzt das neue Budget und löscht den Fehler des Budgetfeldes.
     */
    fun onBudgetChange(newBudget: String) {
        _uiState.update { it.copy(budget = newBudget, budgetError = null) }
    }

    /**
     * Setzt das neue Auslosungsdatum und löscht den Fehler des Datumsfeldes.
     */
    fun onDrawDayChange(newDrawDay: String) {
        _uiState.update { it.copy(drawDay = newDrawDay, drawDayError = null) }
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
        _uiState.update { it.copy(drawType = newDrawType) }
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
            _uiState.update { it.copy(generalError = R.string.error_group_not_loaded_cannot_save) }
            return
        }

        _uiState.update {
            it.copy(isSaving = true, nameError = null, budgetError = null, drawDayError = null, generalError = null)
        }

        val input = GiftGroupInput(
            name = currentState.name,
            budget = currentState.budget,
            drawDay = currentState.drawDay,
            drawType = currentState.drawType,
            // Foto neu, geändert oder entfernt: der aktuelle Stand des Formulars wird gespeichert
            photoPath = currentState.photo.path,
        )

        viewModelScope.launch {
            try {
                when (val result = saveGiftGroup(groupId, input)) {
                    is GiftGroupResult.Success -> {
                        // Ein ersetztes oder entferntes altes Foto erst nach erfolgreichem Speichern löschen
                        photoDraft.commit(savedPath = currentState.photo.path)
                        _uiState.update { it.copy(isSaving = false, savedGroupId = result.giftGroup.id) }
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
                AppLog.e(TAG, "Failed to save the group", e)
                _uiState.update { it.copy(isSaving = false, generalError = saveErrorMessage(e, R.string.error_group_save)) }
            }
        }
    }

    // =========================================================================
    // FOTO (gleicher Ablauf wie beim Teilnehmerfoto; Dateien verwaltet der PhotoDraft)
    // =========================================================================

    /** Ändert das Fotofeld des Formulars. */
    private fun updatePhoto(transform: (PhotoFieldUiState) -> PhotoFieldUiState) {
        _uiState.update { it.copy(photo = transform(it.photo)) }
    }

    /**
     * Übernimmt ein neu aufgenommenes Foto ins Formular. Es wird sofort in den App-Speicher verschoben (notfalls kopiert),
     * aber erst beim Speichern der Gruppe in der Datenbank eingetragen.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            val photo = photoDraft.capture(tempFile, _uiState.value.photo)
            updatePhoto { photo }
        }
    }

    /** Entfernt das Foto aus dem Formular. Ein gespeichertes Foto wird erst beim Speichern gelöscht. */
    fun onRemovePhoto() {
        photoDraft.discard(_uiState.value.photo.path)
        updatePhoto { it.withoutPhoto() }
    }

    /**
     * Formular wird ohne Speichern verlassen: ein neu aufgenommenes, noch nicht gespeichertes Foto löschen.
     */
    fun onDiscard() {
        // Während des Speicherns gehört das Foto womöglich schon zur gespeicherten Gruppe
        if (_uiState.value.isSaving) return
        photoDraft.discard(_uiState.value.photo.path)
    }

    companion object {
        internal const val KEY_COMMITTED_PHOTO = "group_form_committed_photo"

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

