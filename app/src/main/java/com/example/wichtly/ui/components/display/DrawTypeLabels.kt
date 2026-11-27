package com.example.wichtly.ui.components.display

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType

/** Display name of the draw type (e.g. "Classic"), resolved with stringResource(). */
@StringRes
fun DrawType.labelRes(): Int = when (this) {
    DrawType.NORMAL -> R.string.draw_type_normal
    DrawType.JUNK -> R.string.draw_type_junk
}

/** Short explanation of the draw type for the selection cards in the group form. */
@StringRes
fun DrawType.descriptionRes(): Int = when (this) {
    DrawType.NORMAL -> R.string.draw_type_normal_description
    DrawType.JUNK -> R.string.draw_type_junk_description
}
