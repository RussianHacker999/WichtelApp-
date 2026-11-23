package com.example.wichtly.ui.components.photo

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
 * Zustandsloser Dialog zur Auswahl der Foto-Aktion (Foto aufnehmen / Foto entfernen).
 * Für Gruppen- und Teilnehmerfotos identisch; es gibt keine Galerieauswahl.
 *
 * @param area Anwendungsbereich ([PhotoArea.GROUP] oder [PhotoArea.PARTICIPANT]).
 * @param hasPhoto Zeigt "Foto entfernen" an, wenn bereits ein Foto vorhanden ist.
 * @param onTakePhoto Startet die Kameraaufnahme.
 * @param onRemovePhoto Entfernt das vorhandene Foto.
 * @param onDismiss Schließt den Dialog.
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
        title = "Foto hinzufügen",
        modifier = modifier,
        content = {
            Text(
                text = "Das Foto wird nur auf diesem Gerät gespeichert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            PhotoOption(icon = Icons.Filled.PhotoCamera, label = "Foto aufnehmen", onClick = onTakePhoto)
            if (hasPhoto) {
                PhotoOption(icon = Icons.Filled.Delete, label = "Foto entfernen", onClick = onRemovePhoto, destructive = true)
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

/** Preview 1: Auswahldialog ohne vorhandenes Foto. */
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

/** Preview 2: Auswahldialog mit vorhandenem Foto. */
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
