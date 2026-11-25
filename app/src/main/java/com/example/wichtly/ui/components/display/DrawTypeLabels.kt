package com.example.wichtly.ui.components.display

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType

/** Anzeigename der Wichtelart (z. B. "Klassisch"), aufgelöst mit stringResource(). */
@StringRes
fun DrawType.labelRes(): Int = when (this) {
    DrawType.NORMAL -> R.string.draw_type_normal
    DrawType.JUNK -> R.string.draw_type_junk
}

/** Kurze Erklärung der Wichtelart für die Auswahlkarten im Gruppenformular. */
@StringRes
fun DrawType.descriptionRes(): Int = when (this) {
    DrawType.NORMAL -> R.string.draw_type_normal_description
    DrawType.JUNK -> R.string.draw_type_junk_description
}
