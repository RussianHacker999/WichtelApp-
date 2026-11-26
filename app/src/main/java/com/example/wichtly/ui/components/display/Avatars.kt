package com.example.wichtly.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/** Standardgröße eines Avatars in Listenkarten. */
val AvatarSize = 42.dp

/** Initialen für einen Avatar: Anfangsbuchstaben der ersten beiden Wörter („Anna Ludwig“ → „AL“), ohne Namen „?“. */
fun initialsOf(name: String?): String {
    val parts = name.orEmpty().trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

@Composable
private fun AvatarCircle(size: Dp, modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Runder Avatar: das Foto, falls vorhanden, sonst die Initialen von [name]. */
@Composable
fun InitialsAvatar(
    name: String?,
    modifier: Modifier = Modifier,
    photoFile: File? = null,
    size: Dp = AvatarSize,
) {
    if (photoFile != null) {
        PhotoThumbnail(file = photoFile, modifier = modifier.size(size))
    } else {
        AvatarCircle(size, modifier) {
            Text(
                text = initialsOf(name),
                style = if (size > 48.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Runder Avatar mit einer Positionsnummer (Wünsche, Schenker einer Auslosung). */
@Composable
fun NumberAvatar(number: Int, modifier: Modifier = Modifier, size: Dp = AvatarSize) {
    AvatarCircle(size, modifier) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Runder Avatar mit einem Icon (Auslosungen, Historie). */
@Composable
fun IconAvatar(icon: ImageVector, modifier: Modifier = Modifier, size: Dp = AvatarSize) {
    AvatarCircle(size, modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF15392E)
@Composable
private fun AvatarsPreview() {
    WichtlyTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InitialsAvatar(name = "Anna Ludwig")
            InitialsAvatar(name = "Mara", size = 56.dp)
            NumberAvatar(number = 3)
            IconAvatar(icon = Icons.Filled.Redeem)
        }
    }
}
