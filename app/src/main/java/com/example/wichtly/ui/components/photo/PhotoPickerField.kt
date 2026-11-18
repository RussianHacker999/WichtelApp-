package com.example.wichtly.ui.components.photo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser UI-Baustein zur Darstellung des Foto-Auswahlfeldes in Formularen oder Dialogen.
 *
 * Warum zustandslos?
 * Gemäß State Hoisting kommen der Speicherzustand ([hasPhoto]) und Interaktions-Callbacks ([onSelectClick])
 * vollständig von der übergeordneten Ebene. Es findet hier noch kein Bildladen statt.
 *
 * @param area Der Anwendungsbereich ([PhotoArea.GROUP] oder [PhotoArea.PARTICIPANT]) für projektspezifische Texte.
 * @param hasPhoto Gibt an, ob aktuell ein Foto hinterlegt ist.
 * @param onSelectClick Callback beim Klick auf den "Auswählen"- bzw. "Ändern"-Button.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun PhotoPickerField(
    area: PhotoArea,
    hasPhoto: Boolean,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = area.fieldTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Runder Avatar-/Icon-Platzhalter
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (hasPhoto) "✓" else "＋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Titel & Hilfetext
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (hasPhoto) "Foto gespeichert" else area.emptyTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasPhoto) "Tippe auf Ändern, um ein neues Foto aufzunehmen." else area.emptyHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Aktionsbutton (Auswählen / Ändern)
                OutlinedButton(onClick = onSelectClick) {
                    Text(text = if (hasPhoto) "Ändern" else "Auswählen")
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Gruppenfoto-Feld im leeren Zustand.
 */
@Preview(showBackground = true)
@Composable
private fun PhotoPickerFieldGroupEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(
            area = PhotoArea.GROUP,
            hasPhoto = false,
            onSelectClick = {}
        )
    }
}

/**
 * Preview 2: Teilnehmerfoto-Feld im leeren Zustand.
 */
@Preview(showBackground = true)
@Composable
private fun PhotoPickerFieldParticipantEmptyPreview() {
    WichtlyTheme {
        PhotoPickerField(
            area = PhotoArea.PARTICIPANT,
            hasPhoto = false,
            onSelectClick = {}
        )
    }
}

/**
 * Preview 3: Teilnehmerfoto-Feld mit hinterlegtem Foto.
 */
@Preview(showBackground = true)
@Composable
private fun PhotoPickerFieldParticipantWithPhotoPreview() {
    WichtlyTheme {
        PhotoPickerField(
            area = PhotoArea.PARTICIPANT,
            hasPhoto = true,
            onSelectClick = {}
        )
    }
}
