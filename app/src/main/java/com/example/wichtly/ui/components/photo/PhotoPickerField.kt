package com.example.wichtly.ui.components.photo

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.FieldLabel
import com.example.wichtly.ui.components.WichtlyTextButton
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/**
 * Zustandsloser UI-Baustein zur Darstellung des Foto-Auswahlfeldes in Formularen oder Dialogen.
 *
 * Warum zustandslos?
 * Gemäß State Hoisting kommen der Speicherzustand ([hasPhoto]), die optionale Datei ([photoFile])
 * und Interaktions-Callbacks ([onSelectClick]) vollständig von der übergeordneten Ebene.
 *
 * @param area Der Anwendungsbereich ([PhotoArea.GROUP] oder [PhotoArea.PARTICIPANT]). Das Feld sieht für
 *   beide gleich aus; der Parameter bleibt für die Kennzeichnung erhalten.
 * @param hasPhoto Gibt an, ob aktuell ein Foto hinterlegt ist.
 * @param onSelectClick Callback beim Klick auf das Foto oder auf "Foto hinzufügen" bzw. "Foto ändern".
 * @param modifier Optionaler Layout-Modifier.
 * @param photoFile Optionale Bilddatei zur Anzeige im [PhotoThumbnail].
 * @param enabled Deaktiviert das Feld, z. B. während gespeichert wird.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun PhotoPickerField(
    area: PhotoArea,
    hasPhoto: Boolean,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoFile: File? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(text = stringResource(R.string.photo_label))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            // Runder Foto-Thumbnail oder Platzhalter
            PhotoThumbnail(
                file = if (hasPhoto) photoFile else null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onSelectClick),
            )
            // kein eigener Titel/Hilfetext mehr: das Label "Foto" steht über der Zeile
            // Aktionsbutton (Foto hinzufügen / Foto ändern)
            WichtlyTextButton(
                text = stringResource(if (hasPhoto) R.string.photo_change else R.string.photo_add),
                onClick = onSelectClick,
                enabled = enabled,
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Gruppenfoto-Feld im leeren Zustand.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldGroupEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.GROUP, hasPhoto = false, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}

/**
 * Preview 2: Teilnehmerfoto-Feld im leeren Zustand.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldParticipantEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.PARTICIPANT, hasPhoto = false, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}

/**
 * Preview 3: Teilnehmerfoto-Feld mit hinterlegtem Foto.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldParticipantWithPhotoPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.PARTICIPANT, hasPhoto = true, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}
