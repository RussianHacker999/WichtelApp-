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

/*
 * Creates consistent buttons for the app.
 * Buttons are pill-shaped and use [ButtonHeight], or [CompactButtonHeight] for horizontal padding when compact.
 */
private val ButtonHeight = 48.dp
private val CompactButtonHeight = 40.dp

/**
 * Returns the padding for a button based on its compactnes.
 * set the horizontal padding value on 10.dp, if compact is true, otherwise 12.dp
 */
private fun buttonPadding(compact: Boolean) = PaddingValues(horizontal = if (compact) 10.dp else 12.dp)

/**
 * Shows the button label and, when icon is set the icon beside.
 * While loading, a spinner replaces the icon and the label stays visible.
 * The font size of the label gets smaller to fit in the button if needed.
 *
 * @param text The label shown on the button.
 * @param icon The optional icon shown beside the label.
 * @param loading Whether to show a loading spinner instead of the icon - loading state
 * @param loadingColor The spinner color while loading.
 */
@Composable
private fun ButtonContent(text: String, icon: ImageVector?, loading: Boolean, loadingColor: Color) {
    if (loading) {
        // elemnet from Material 3 - loading circular
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
    // To prevent the text from being cut off as much as possible,
    // autoSize gradually reduces its size to 11sp if the button is narrow
    // (two buttons of equal width, large system font).
    // If that is not enough, the text is truncated with “…” (TextOverflow.Ellipsis).
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

/** A gold pill-shaped button for the main action on a screen or in a dialog.
 * use ButtonContent
 *
 * @param text The label shown on the button.
 * @param onClick The function to be called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param icon The optional icon shown beside the label.
 * @param enabled Whether the button is enabled.
 * @param loading Whether to show a loading spinner instead of the icon - loading state
 * @param compact Whether the button should have a small horizontal padding
 */
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

/** An outlined pill-shaped button for a secondary action next to a [PrimaryButton].
 * use ButtonContent
 *
 * @param text The label shown on the button.
 * @param onClick The function to be called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param icon The optional icon shown beside the label.
 * @param enabled Whether the button is enabled.
 * @param loading Whether to show a loading spinner instead of the icon - loading state
 * @param compact Whether the button should have a small horizontal padding
 */
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

/** A red pill-shaped button for confirming a destructive action.
 * use ButtonContent
 *
 * @param text The label shown on the button.
 * @param onClick The function to be called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param loading Whether to show a loading spinner instead of the icon - loading state
 * @param compact Whether the button should have a small horizontal padding
 */
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

/** A gold text button, for example for cancel actions in dialogs or text links.
 * use ButtonContent
 *
 * @param text The label shown on the button.
 * @param onClick The function to be called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param icon The optional icon shown beside the label.
 */
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
 * A small, gold-outlined button for an action within a section, such as adding a participant.
 * This keeps the bottom action bar free for the screen's main action.
 * use ButtonContent
 *
 * @param text The label shown on the button.
 * @param onClick The function to be called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param icon The optional icon shown beside the label - default: Icons.Filled.Add
 * @param enabled Whether the button is enabled.
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
