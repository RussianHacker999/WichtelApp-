package com.example.wichtly.ui.components.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

private val ButtonHeight = 48.dp
private val CompactButtonHeight = 40.dp

private fun buttonPadding(compact: Boolean) =
    PaddingValues(horizontal = if (compact) 10.dp else 12.dp)

@Composable
private fun ButtonContent(text: String, icon: ImageVector?, loading: Boolean, loadingColor: Color) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = loadingColor,
        )
        Spacer(modifier = Modifier.width(Spacing.s))
    } else if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(Spacing.xs))
    }
    // So the label is not cut off, autoSize shrinks it step by step down to 11sp if the button is
    // narrow (two buttons of equal width, large system font). If that is still not enough, it is
    // shortened with "…" (TextOverflow.Ellipsis).
    val labelStyle = MaterialTheme.typography.labelLarge
    BasicText(
        text = text,
        style = labelStyle.copy(color = LocalContentColor.current),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = labelStyle.fontSize, stepSize = 0.5.sp),
    )
}

/** Gold pill button for the main action of a screen or dialog. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(if (compact) CompactButtonHeight else ButtonHeight),
        shape = CircleShape,
        contentPadding = buttonPadding(compact),
    ) {
        ButtonContent(text, icon, loading, MaterialTheme.colorScheme.onPrimary)
    }
}

/** Outlined pill button for the secondary action next to the [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) CompactButtonHeight else ButtonHeight),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        contentPadding = buttonPadding(compact),
    ) {
        ButtonContent(text, icon, loading = false, loadingColor = MaterialTheme.colorScheme.onBackground)
    }
}

/** Red pill button to confirm a delete. */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(if (compact) CompactButtonHeight else ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        contentPadding = buttonPadding(compact),
    ) {
        ButtonContent(text, icon = null, loading = loading, loadingColor = MaterialTheme.colorScheme.onError)
    }
}

/** Gold text button, e.g. for "Cancel" in dialogs and for links in text. */
@Composable
fun WichtlyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        ButtonContent(text, icon, loading = false, loadingColor = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Small, gold-outlined pill for an action that belongs to a section (e.g. "+ Add" next to the
 * participant list). Keeps the bottom action bar free for the one main action of the screen.
 */
@Composable
fun SectionActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Filled.Add,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(SectionActionHeight),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(start = 10.dp, end = 12.dp),
    ) {
        ButtonContent(text, icon, loading = false, loadingColor = MaterialTheme.colorScheme.primary)
    }
}

private val SectionActionHeight = 34.dp

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun ButtonsPreview() {
    WichtlyTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Losen", onClick = {}, icon = Icons.Filled.Redeem)
            PrimaryButton(text = "Wird ausgelost...", onClick = {}, loading = true)
            SecondaryButton(text = "Abbrechen", onClick = {})
            DangerButton(text = "Löschen", onClick = {}, compact = true)
            WichtlyTextButton(text = "Einstellungen öffnen", onClick = {})
            SectionActionButton(text = "Hinzufügen", onClick = {})
        }
    }
}
