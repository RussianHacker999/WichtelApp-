package com.example.wichtly.ui.components.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
 * Dekodiert die übergebene Datei ([file]) asynchron im Hintergrund (Dispatchers.IO) mit Downsampling (inSampleSize)
 * und korrigiert die Bildausrichtung anhand der EXIF-Metadaten.
 * Falls die Datei `null` ist, nicht existiert oder nicht dekodierbar ist, wird ein Kamera-Symbol als Platzhalter gezeigt.
 *
 * @param file Die anzuzeigende Bilddatei (null = Platzhalter).
 * @param modifier Optionaler Layout-Modifier.
 * @param shape Die Form des Rahmens (Standard ist [CircleShape]).
 */
@Composable
fun PhotoThumbnail(
    file: File?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = file) {
        value = withContext(Dispatchers.IO) {
            if (file == null || (!file.exists())) {
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
                    val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)

                    if (decodedBitmap == null) {
                        null
                    } else {
                        // 4. EXIF-Ausrichtung korrigieren
                        val rotatedBitmap = rotateBitmapIfNeeded(decodedBitmap, file.absolutePath)
                        rotatedBitmap.asImageBitmap()
                    }
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
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Kein Foto",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Liest die EXIF-Ausrichtung der Datei und dreht das [Bitmap] bei Bedarf speicherschonend.
 * Altes Zwischen-Bitmap wird recycelt, falls ein neues roteriertes Bitmap erzeugt wurde.
 */
private fun rotateBitmapIfNeeded(bitmap: Bitmap, filePath: String): Bitmap {
    return try {
        val exifInterface = ExifInterface(filePath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees == 0f) {
            bitmap
        } else {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        }
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("PhotoThumbnail", "Fehler beim Lesen der EXIF-Daten: $filePath", e)
        bitmap
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
