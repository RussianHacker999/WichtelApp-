package com.example.wichtly.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.display.Sparkle
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/** Zentrierter Ladekreis mit kurzem Text. Auf allen Screens gleich. */
@Composable
fun LoadingState(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.l, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Zentriertes Icon, Titel, Text und optionaler Button. Für Fehler-, Nicht-gefunden- und Leer-Zustände.
 *
 * @param isError Zeigt das Icon in der Fehlerfarbe.
 * @param fillMaxSize false, wenn der Zustand innerhalb einer scrollenden Liste steht (leere Listen).
 */
@Composable
fun MessageState(
    title: String,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    fillMaxSize: Boolean = true,
) {
    Column(
        modifier = modifier
            .then(if (fillMaxSize) Modifier.fillMaxSize() else Modifier)
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m, Alignment.CenterVertically),
    ) {
        StateIcon(icon = icon, isError = isError)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        if (actionLabel != null && onAction != null) {
            PrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.padding(top = Spacing.xs))
        }
    }
}

@Composable
private fun StateIcon(icon: ImageVector, isError: Boolean) {
    val tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(34.dp))
        }
        Sparkle(size = 20.dp, rotationDegrees = 18f, modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-14).dp))
        Sparkle(size = 9.dp, alpha = 0.7f, modifier = Modifier.align(Alignment.BottomStart).offset(x = (-12).dp, y = (-2).dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun LoadingStatePreview() {
    WichtlyTheme { LoadingState(text = "Einen Moment, wird ausgepackt …") }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun MessageStatePreview() {
    WichtlyTheme {
        MessageState(
            title = "Diese Gruppe ist verschwunden",
            text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
            icon = Icons.Outlined.ErrorOutline,
            isError = true,
            actionLabel = "Zurück zur Übersicht",
            onAction = {},
        )
    }
}
