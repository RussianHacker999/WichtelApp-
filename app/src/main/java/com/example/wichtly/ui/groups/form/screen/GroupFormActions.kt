package com.example.wichtly.ui.groups.form.screen

import com.example.wichtly.domain.model.group.DrawType
import java.io.File

/**
 * User actions of the group form: input, save, back and group photo.
 *
 * Bundles the callbacks so the screen does not take more than ten single parameters. The route fills
 * them with ViewModel methods and navigation, without default values, so no action is forgotten.
 */
data class GroupFormActions(
    /** The group name changed. */
    val onNameChange: (String) -> Unit,
    /** The budget changed. */
    val onBudgetChange: (String) -> Unit,
    /** The draw day changed. */
    val onDrawDayChange: (String) -> Unit,
    /** The draw type changed. */
    val onDrawTypeChange: (DrawType) -> Unit,
    /** Starts saving. */
    val onSaveClick: () -> Unit,
    /** Navigates back. */
    val onBack: () -> Unit,
    /** A new group photo was captured (temporary file of the camera). */
    val onPhotoCaptured: (File) -> Unit,
    /** Removes the group photo. */
    val onRemovePhoto: () -> Unit,
)
