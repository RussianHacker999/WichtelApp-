package com.example.wichtly.ui.photo.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.components.controls.FieldLabel
import com.example.wichtly.ui.components.controls.WichtlyTextButton
import com.example.wichtly.ui.components.display.PhotoThumbnail
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

private const val TAG = "PhotoField"

/**
 * Das komplette Fotofeld eines Formulars, der einzige Einstieg in das Foto-Paket für Bildschirme.
 *
 * Zeigt Vorschau und "Foto hinzufügen/ändern", darunter den Hinweis bei abgelehnter Kamera (mit Link zu den
 * Einstellungen) und ggf. einen Fehler. Den Ablauf dahinter (Quelle → Berechtigung → Kamera) steuert das Feld
 * selbst ([PhotoFlowState], [PhotoCaptureFlow]); nach außen gehen nur die beiden Ergebnisse.
 *
 * @param photo Fachlicher Zustand aus dem ViewModel (Pfad, Vorschaudatei, Speicherfehler).
 * @param onPhotoCaptured Neues Foto aufgenommen (temporäre Datei der Kamera); das ViewModel übernimmt es.
 * @param onRemovePhoto Foto entfernen.
 * @param modifier Optionaler Layout-Modifier.
 * @param enabled Deaktiviert das Feld, z. B. während gespeichert wird.
 */
@Composable
fun PhotoField(
    photo: PhotoFieldUiState,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val flow = rememberPhotoFlowState()
    PhotoFieldContent(photo = photo, flow = flow, modifier = modifier, enabled = enabled)
    PhotoCaptureFlow(flow = flow, hasPhoto = photo.hasPhoto, onPhotoCaptured = onPhotoCaptured, onRemovePhoto = onRemovePhoto)
}

/** Sichtbarer Teil des Fotofelds (ohne Dialoge), getrennt für die Previews. */
@Composable
private fun PhotoFieldContent(
    photo: PhotoFieldUiState,
    flow: PhotoFlowState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val error = photo.error ?: if (flow.captureFailed) R.string.error_photo_capture else null
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        PhotoPickerField(
            hasPhoto = photo.hasPhoto,
            photoFile = photo.file,
            onSelectClick = flow::open,
            enabled = enabled,
        )
        if (flow.cameraDenied) {
            CameraPermissionDeniedHint(onOpenSettings = { openAppSettings(context) })
        }
        if (error != null) {
            Text(text = stringResource(error), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * Zeile aus Foto-Vorschau und "Foto hinzufügen" bzw. "Foto ändern" (zustandslos).
 *
 * Warum zustandslos?
 * Gemäß State Hoisting kommen der Speicherzustand ([hasPhoto]), die optionale Datei ([photoFile])
 * und Interaktions-Callbacks ([onSelectClick]) vollständig von der übergeordneten Ebene.
 *
 * @param hasPhoto Gibt an, ob aktuell ein Foto hinterlegt ist.
 * @param onSelectClick Callback beim Klick auf das Foto oder auf "Foto hinzufügen" bzw. "Foto ändern".
 * @param modifier Optionaler Layout-Modifier.
 * @param photoFile Optionale Bilddatei zur Anzeige im [PhotoThumbnail].
 * @param enabled Deaktiviert das Feld, z. B. während gespeichert wird.
 */
@Composable
private fun PhotoPickerField(
    hasPhoto: Boolean,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoFile: File? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
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

/** Öffnet die System-Einstellungen dieser App (um den Kamerazugriff doch noch zu erlauben). */
private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        AppLog.w(TAG, "Could not open the app settings", e)
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/** Preview 1: Fotofeld ohne Foto. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldEmptyPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(), flow = PhotoFlowState(), modifier = Modifier.padding(16.dp))
    }
}

/** Preview 2: Fotofeld nach abgelehnter Kamera (Hinweis mit Einstellungen-Link). */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldCameraDeniedPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(), flow = PhotoFlowState(cameraDenied = true), modifier = Modifier.padding(16.dp))
    }
}

/** Preview 3: Fotofeld mit hinterlegtem Foto. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldWithPhotoPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(path = "photos/preview.jpg"), flow = PhotoFlowState(), modifier = Modifier.padding(16.dp))
    }
}
