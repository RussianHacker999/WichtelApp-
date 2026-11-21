package com.example.wichtly.ui.components.photo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Auswahl von Foto-Aktionen (Kamera aufnehmen / Foto entfernen).
 *
 * Warum zustandslos?
 * Die Anzeige und Steuerung erfolgt rein über Parameter und Lambda-Callbacks. Es gibt keine Galerieauswahl;
 * ausschließlich die Kameraaufzeichnung wird angeboten.
 *
 * @param area Der Anwendungsbereich ([PhotoArea.GROUP] oder [PhotoArea.PARTICIPANT]).
 * @param hasPhoto Zeigt die "Foto entfernen"-Aktion an, wenn `hasPhoto == true`.
 * @param onTakePhoto Callback zum Auslösen der Kameraaufnahme.
 * @param onRemovePhoto Callback zum Entfernen des vorhandenen Fotos.
 * @param onDismiss Callback beim Schließen oder Abbrechen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun PhotoSourceDialog(
    area: PhotoArea,
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = area.dialogTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Das Foto wird nur auf diesem Gerät gespeichert.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Option: Foto aufnehmen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTakePhoto),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column(
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                text = "Foto aufnehmen",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Die Kamera wird erst jetzt angefragt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Option: Foto entfernen (nur sichtbar wenn hasPhoto == true)
                if (hasPhoto) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onRemovePhoto),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Column(
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Foto entfernen",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ohne Foto fortfahren.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Abbrechen")
            }
        },
        modifier = modifier
    )
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Gruppenfoto-Auswahldialog ohne vorhandenes Foto.
 */
@Preview
@Composable
private fun PhotoSourceDialogGroupEmptyPreview() {
    WichtlyTheme {
        PhotoSourceDialog(
            area = PhotoArea.GROUP,
            hasPhoto = false,
            onTakePhoto = {},
            onRemovePhoto = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 2: Teilnehmerfoto-Auswahldialog mit vorhandenem Foto.
 */
@Preview
@Composable
private fun PhotoSourceDialogParticipantWithPhotoPreview() {
    WichtlyTheme {
        PhotoSourceDialog(
            area = PhotoArea.PARTICIPANT,
            hasPhoto = true,
            onTakePhoto = {},
            onRemovePhoto = {},
            onDismiss = {}
        )
    }
}
