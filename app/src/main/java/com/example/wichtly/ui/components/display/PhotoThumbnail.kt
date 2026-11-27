package com.example.wichtly.ui.components.display

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PhotoThumbnail"

/**
 * Stateless building block that shows a preview of an image file.
 *
 * Decodes [file] asynchronously in the background (Dispatchers.IO) with downsampling (inSampleSize)
 * and corrects the orientation from the EXIF metadata. The composable keeps no state of its own
 * beyond the decoded bitmap, so callers decide which file is shown.
 * If the file is null, does not exist or cannot be decoded, a camera icon is shown as a placeholder.
 *
 * @param file the image file to show (null = placeholder).
 * @param shape shape of the frame (default is [CircleShape]).
 */
@Composable
fun PhotoThumbnail(file: File?, modifier: Modifier = Modifier, shape: Shape = CircleShape) {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = file) {
        value = withContext(Dispatchers.IO) {
            if (file == null || (!file.exists())) {
                null
            } else {
                try {
                    // 1. Read the dimensions of the file without decoding it completely
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)

                    // 2. Compute inSampleSize: halve as often as both sides still have at least 300 px afterwards
                    val targetSize = 300
                    var sampleSize = 1
                    while (options.outWidth / (sampleSize * 2) >= targetSize &&
                        options.outHeight / (sampleSize * 2) >= targetSize
                    ) {
                        sampleSize *= 2
                    }

                    // 3. Decode the image with the reduced size to save memory
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)

                    if (decodedBitmap == null) {
                        null
                    } else {
                        // 4. Correct the EXIF orientation
                        val rotatedBitmap = rotateBitmapIfNeeded(decodedBitmap, file.absolutePath)
                        rotatedBitmap.asImageBitmap()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLog.w(TAG, "Could not decode photo, showing placeholder: ${file.name}", e)
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
                contentDescription = stringResource(R.string.photo_preview_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = stringResource(R.string.photo_none_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Reads the EXIF orientation of the file and rotates the [Bitmap] if necessary.
 *
 * The old intermediate bitmap is recycled if a new rotated bitmap was created.
 */
private fun rotateBitmapIfNeeded(bitmap: Bitmap, filePath: String): Bitmap = try {
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
            true,
        )
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        rotatedBitmap
    }
} catch (e: Exception) {
    // No rethrow of CancellationException: this is a plain function, not a coroutine
    AppLog.w(TAG, "Could not read EXIF orientation, showing photo unrotated: ${filePath.substringAfterLast('/')}", e)
    bitmap
}

/**
 * Preview: photo thumbnail placeholder (no file).
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
