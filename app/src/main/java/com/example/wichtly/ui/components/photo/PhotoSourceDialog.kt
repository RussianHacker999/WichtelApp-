package com.example.wichtly.ui.components.photo

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.theme.Spacing
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
@Suppress("UNUSED_PARAMETER")
@Composable
fun PhotoSourceDialog(
    area: PhotoArea,
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.PhotoCamera,
        title = stringResource(R.string.photo_add),
        modifier = modifier,
        content = {
            Text(
                text = stringResource(R.string.photo_local_only_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            // Option: Foto aufnehmen
            PhotoOption(icon = Icons.Filled.PhotoCamera, label = stringResource(R.string.photo_take), onClick = onTakePhoto)
            // Option: Foto entfernen (nur sichtbar wenn hasPhoto == true)
            if (hasPhoto) {
                PhotoOption(icon = Icons.Filled.Delete, label = stringResource(R.string.photo_remove), onClick = onRemovePhoto, destructive = true)
            }
        },
        // Die Optionen sind selbst die Aktionen; unten bleibt nur "Abbrechen".
        confirmLabel = null,
        onConfirm = {},
    )
}

@Composable
private fun PhotoOption(icon: ImageVector, label: String, onClick: () -> Unit, destructive: Boolean = false) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) color else MaterialTheme.colorScheme.primary,
            )
            Text(text = label, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
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
