package com.example.wichtly.ui.components.controls

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
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
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * The one dialog layout of the app: round icon, serif title, optional text and content,
 * then "dismiss" (text button) and "confirm" (pill button) on the right.
 *
 * @param destructive Shows the icon and confirm button in red (delete dialogs).
 * @param confirmLabel null leaves only the dismiss button (e.g. when the content itself holds the actions).
 * @param dismissLabel null hides the dismiss button.
 */
@Composable
fun WichtlyDialog(
    onDismissRequest: () -> Unit,
    icon: ImageVector,
    title: String,
    confirmLabel: String?,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissLabel: String? = stringResource(R.string.common_cancel),
    onDismiss: () -> Unit = onDismissRequest,
    destructive: Boolean = false,
    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = { DialogIcon(icon = icon, destructive = destructive) },
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        },
        text = if (text == null && content == null) null else {
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                ) {
                    if (text != null) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    content?.invoke(this)
                }
            }
        },
        confirmButton = {
            if (confirmLabel == null) {
                WichtlyTextButton(text = dismissLabel ?: stringResource(R.string.common_cancel), onClick = onDismiss, enabled = dismissEnabled)
            } else if (destructive) {
                DangerButton(text = confirmLabel, onClick = onConfirm, enabled = confirmEnabled, loading = confirmLoading, compact = true)
            } else {
                PrimaryButton(text = confirmLabel, onClick = onConfirm, enabled = confirmEnabled, loading = confirmLoading, compact = true)
            }
        },
        dismissButton = dismissLabel?.takeIf { confirmLabel != null }?.let { label ->
            { WichtlyTextButton(text = label, onClick = onDismiss, enabled = dismissEnabled) }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DialogIcon(icon: ImageVector, destructive: Boolean) {
    val tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Preview
@Composable
private fun WichtlyDialogPreview() {
    WichtlyTheme {
        WichtlyDialog(
            onDismissRequest = {},
            icon = Icons.Filled.Visibility,
            title = "Kamera erlauben",
            text = "Wichtly braucht Zugriff auf die Kamera, um ein Foto aufzunehmen.",
            confirmLabel = "Erlauben",
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun WichtlyDialogDestructivePreview() {
    WichtlyTheme {
        WichtlyDialog(
            onDismissRequest = {},
            icon = Icons.Filled.Delete,
            title = "Wunsch löschen",
            text = "Möchtest du den Wunsch „Warme Wollsocken“ wirklich löschen?",
            confirmLabel = "Löschen",
            onConfirm = {},
            destructive = true,
        )
    }
}
