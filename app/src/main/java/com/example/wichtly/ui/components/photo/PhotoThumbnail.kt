package com.example.wichtly.ui.components.photo

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Zustandsloser UI-Baustein zur Vorschau-Darstellung eines Bildes aus einer Datei.
 *
 * Warum zustandslos?
 * Dekodiert die übergebene Datei ([file]) asynchron im Hintergrund (Dispatchers.IO) mit Downsampling (inSampleSize),
 * um Speicherdruck und Abstürze zu vermeiden. Falls die Datei `null` ist, nicht existiert oder nicht dekodierbar ist,
 * wird ein eleganter Platzhalter ("＋") gerendert.
 *
 * @param file Die anzuzeigende Bilddatei (null = Platzhalter).
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun PhotoThumbnail(
    file: File?,
    modifier: Modifier = Modifier,
) {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = file) {
        value = withContext(Dispatchers.IO) {
            if (file == null || !file.exists()) {
                null
            } else {
                try {
                    // 1. Abmessungen der Datei ohne vollständiges Dekodieren prüfen
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)

                    // 2. inSampleSize für Downsampling auf ca. 160dp / 300px berechnen
                    val targetSize = 300
                    var sampleSize = 1
                    while (options.outWidth / (sampleSize * 2) >= targetSize &&
                        options.outHeight / (sampleSize * 2) >= targetSize
                    ) {
                        sampleSize *= 2
                    }

                    // 3. Bild speicherschonend dekodieren
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                    bitmap?.asImageBitmap()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("PhotoThumbnail", "Fehler beim Dekodieren des Fotos: ${file.absolutePath}", e)
                    null
                }
            }
        }
    }

    val imageBitmap = bitmapState.value

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Foto-Vorschau",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "＋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Foto-Thumbnail Platzhalter (ohne Datei).
 */
@Preview
@Composable
private fun PhotoThumbnailPlaceholderPreview() {
    WichtlyTheme {
        PhotoThumbnail(
            file = null,
            modifier = Modifier.size(56.dp),
        )
    }
}
