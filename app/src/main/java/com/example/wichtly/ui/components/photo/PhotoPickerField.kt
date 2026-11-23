package com.example.wichtly.ui.components.photo

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
 * Zustandsloses Fotofeld für Formulare und Dialoge. Gruppe und Teilnehmer sehen gleich aus:
 * Label "Foto", runde Vorschau (oder Kamera-Platzhalter) und "Foto hinzufügen" / "Foto ändern".
 *
 * @param area Anwendungsbereich ([PhotoArea.GROUP] oder [PhotoArea.PARTICIPANT]).
 * @param hasPhoto Gibt an, ob aktuell ein Foto hinterlegt ist.
 * @param onSelectClick Öffnet den Foto-Ablauf.
 * @param modifier Optionaler Layout-Modifier.
 * @param photoFile Optionale Bilddatei für die Vorschau.
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
        FieldLabel(text = "Foto")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            PhotoThumbnail(
                file = if (hasPhoto) photoFile else null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onSelectClick),
            )
            WichtlyTextButton(
                text = if (hasPhoto) "Foto ändern" else "Foto hinzufügen",
                onClick = onSelectClick,
                enabled = enabled,
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/** Preview 1: Fotofeld ohne Foto (Gruppe). */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldGroupEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.GROUP, hasPhoto = false, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}

/** Preview 2: Fotofeld ohne Foto (Teilnehmer). */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldParticipantEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.PARTICIPANT, hasPhoto = false, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}

/** Preview 3: Fotofeld mit hinterlegtem Foto. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoPickerFieldParticipantWithPhotoPreview() {
    WichtlyTheme {
        PhotoPickerField(area = PhotoArea.PARTICIPANT, hasPhoto = true, onSelectClick = {}, modifier = Modifier.padding(16.dp))
    }
}
