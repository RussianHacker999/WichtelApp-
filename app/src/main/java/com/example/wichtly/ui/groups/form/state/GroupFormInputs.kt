package com.example.wichtly.ui.groups.form.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.ui.components.display.formatCentsForInput
import com.example.wichtly.ui.components.display.formatDateForInput
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState

/**
 * The input of the form as it is stored in the SavedStateHandle.
 *
 * Error texts and the loading state are not saved; they are created again on the next save.
 */
internal data class FormInputs(val name: String, val budget: String, val drawDay: String, val drawType: DrawType, val photoPath: String?) {
    fun applyTo(state: GroupFormUiState): GroupFormUiState = state.copy(
        name = name,
        budget = budget,
        drawDay = drawDay,
        drawType = drawType,
        photo = state.photo.copy(path = photoPath),
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

        fun of(state: GroupFormUiState) = FormInputs(state.name, state.budget, state.drawDay, state.drawType, state.photo.path)

        /** The input as given by the saved group (edit mode). */
        fun of(group: GiftGroup) = FormInputs(
            name = group.name,
            budget = formatCentsForInput(group.budgetCents),
            drawDay = formatDateForInput(group.drawDay),
            drawType = group.drawType,
            photoPath = group.photoPath,
        )

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
