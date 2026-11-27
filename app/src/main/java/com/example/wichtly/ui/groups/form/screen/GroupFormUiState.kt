package com.example.wichtly.ui.groups.form.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState

/**
 * UI state of the group form (creating and editing a gift group).
 *
 * All error fields are string resources from strings.xml.
 *
 * @property isLoading in edit mode, the group data is still being loaded from the database.
 * @property isEditMode false = create a new group, true = edit an existing group.
 * @property notFound in edit mode, the requested group does not exist or was deleted.
 * @property loadFailed in edit mode, loading the group failed with an error. The screen then shows
 *   an error state instead of the (empty) form.
 * @property name current text of the group name field.
 * @property budget current text of the budget field (e.g. "25,50").
 * @property drawDay current text of the draw day field (e.g. "24.12.2026").
 * @property drawType selected draw type (DrawType.NORMAL = classic, DrawType.JUNK = junk gift exchange).
 * @property nameError validation or format error of the name field (null = no error).
 * @property budgetError validation or format error of the budget field (null = no error).
 * @property drawDayError validation or format error of the date field (null = no error).
 * @property generalError general error at form level (e.g. "Group not found").
 * @property isSaving a save to the database is running (disables input and buttons).
 * @property savedGroupId one-time event: ID of the saved group; the route navigates and acknowledges
 *   it with onSavedHandled().
 * @property photo photo field of the group (same state as in the participant editor; the UI holds the
 *   photo flow). The path is written to the database only when saving.
 * @property drawDayPassed when editing: the stored draw day is over. The form then explains at the
 *   date field that a new date is needed for a new round.
 */
data class GroupFormUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val notFound: Boolean = false,
    val loadFailed: Boolean = false,
    val name: String = "",
    val budget: String = "",
    val drawDay: String = "",
    val drawType: DrawType = DrawType.NORMAL,
    @StringRes val nameError: Int? = null,
    @StringRes val budgetError: Int? = null,
    @StringRes val drawDayError: Int? = null,
    @StringRes val generalError: Int? = null,
    val isSaving: Boolean = false,
    val savedGroupId: String? = null,
    val photo: PhotoFieldUiState = PhotoFieldUiState(),
    val drawDayPassed: Boolean = false,
)
