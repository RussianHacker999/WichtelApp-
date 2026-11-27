package com.example.wichtly.ui.draws.detail.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wichtly.R
import com.example.wichtly.ui.components.display.Sparkle
import com.example.wichtly.ui.theme.EyebrowStyle
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

// Fixed padding of the gift tag: room for the hole with the ribbon at the top, for the edge at the bottom
private val TagPaddingHorizontal = 20.dp
private val TagPaddingTop = 52.dp
private val TagPaddingBottom = 22.dp

/**
 * Stateless full-screen dialog that shows the drawn assignment (giver -> receiver) as a gift tag:
 * closed at first ("Tap to open"), and after a tap open, including the receiver's wishes.
 *
 * Takes the finished assignment data and only calls [onDismiss] for "Close", "Not me" or a tap
 * outside. The only thing it remembers itself is whether the tag is already open (rememberSaveable).
 *
 * @param assignment the assignment to show, with giver, receiver and wishlist.
 * @param onDismiss closes the gift tag.
 * @param startOpened true opens the tag right away (e.g. for previews).
 */
@Composable
fun DrawResultDialog(
    assignment: DrawAssignmentUiModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    startOpened: Boolean = false,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GiftTagReveal(assignment = assignment, onDismiss = onDismiss, startOpened = startOpened, modifier = modifier)
    }
}

@Composable
private fun GiftTagReveal(
    assignment: DrawAssignmentUiModel,
    onDismiss: () -> Unit,
    startOpened: Boolean,
    modifier: Modifier = Modifier,
) {
    var opened by rememberSaveable(assignment.giverId) { mutableStateOf(startOpened) }
    val tagColor = MaterialTheme.colorScheme.primary
    val onTag = MaterialTheme.colorScheme.onPrimary
    val scrim = Color.Black.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // String of the tag
            Canvas(modifier = Modifier.size(width = 120.dp, height = 64.dp).offset(y = 18.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.08f, -size.height * 0.6f)
                    cubicTo(size.width * 0.25f, size.height * 0.45f, size.width * 0.6f, size.height * 0.15f, size.width * 0.5f, size.height)
                }
                drawPath(path, color = tagColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
            Box {
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .heightIn(min = 320.dp, max = 560.dp)
                        .clip(GiftTagShape)
                        .background(tagColor)
                        .drawWithContent {
                            drawContent()
                            // Hole for the string
                            drawCircle(color = scrim, radius = 8.dp.toPx(), center = Offset(size.width / 2f, 24.dp.toPx()))
                        }
                        .clickable(
                            enabled = !opened,
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.draw_result_open_label),
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { opened = true }
                        .verticalScroll(rememberScrollState())
                        .padding(start = TagPaddingHorizontal, end = TagPaddingHorizontal, top = TagPaddingTop, bottom = TagPaddingBottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(
                        targetState = opened,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "giftTag",
                    ) { isOpen ->
                        if (isOpen) {
                            OpenedTag(assignment = assignment, contentColor = onTag, onDismiss = onDismiss)
                        } else {
                            ClosedTag(giverName = assignment.giverName, contentColor = onTag, onDismiss = onDismiss)
                        }
                    }
                }
                Sparkle(size = 24.dp, rotationDegrees = 14f, modifier = Modifier.align(Alignment.TopStart).offset(x = (-30).dp, y = 64.dp))
                Sparkle(size = 11.dp, rotationDegrees = -20f, alpha = 0.75f, modifier = Modifier.align(Alignment.TopEnd).offset(x = 18.dp, y = 180.dp))
            }
        }
    }
}

/**
 * Closed tag ("For your eyes only").
 *
 * @param onDismiss closes the tag without revealing the result, for the case that the
 *   wrong person is in front of the screen.
 */
@Composable
private fun ClosedTag(giverName: String, contentColor: Color, onDismiss: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.inline)) {
        // The closed tag takes over the role of a separate confirmation ("For your eyes only").
        TagLabel(text = stringResource(R.string.draw_result_only_for_you), color = contentColor)
        Icon(imageVector = Icons.Filled.Redeem, contentDescription = null, tint = contentColor, modifier = Modifier.size(54.dp))
        Text(
            text = stringResource(R.string.draw_result_is_it_you, giverName),
            style = MaterialTheme.typography.headlineSmall.copy(shadow = TagTextShadow),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.draw_result_shield_hint),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        // Wrong person: close without revealing. The button consumes the click,
        // so the tag does not open at the same time.
        OutlinedButton(
            onClick = onDismiss,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, contentColor.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
            modifier = Modifier.padding(top = Spacing.s),
        ) {
            Text(text = stringResource(R.string.draw_result_mistyped), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpenedTag(assignment: DrawAssignmentUiModel, contentColor: Color, onDismiss: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.inline)) {
        TagLabel(text = stringResource(R.string.draw_result_you_gift, assignment.giverName), color = contentColor)
        Text(
            text = assignment.receiverName,
            style = MaterialTheme.typography.displaySmall.copy(shadow = TagTextShadow),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.draw_result_wishlist_of, assignment.receiverName),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
        )
        if (assignment.wishes.isEmpty()) {
            Text(
                text = stringResource(R.string.draw_result_no_wishes),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.tight, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight),
                modifier = Modifier.fillMaxWidth(),
            ) {
                assignment.wishes.forEach { wish ->
                    Surface(shape = MaterialTheme.shapes.small, color = Color.Black.copy(alpha = 0.14f), contentColor = contentColor) {
                        Text(
                            text = wish,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = Spacing.inline, vertical = Spacing.tight),
                        )
                    }
                }
            }
        }
        Button(
            onClick = onDismiss,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(top = Spacing.s),
        ) {
            Text(text = stringResource(R.string.draw_result_close), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TagLabel(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        // Eyebrow on the tag: bold, so it is clearly readable
        style = EyebrowStyle,
        color = color.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
    )
}

/** Light shadow for the serif font on the gold tag (the dark title shadow would be too heavy here). */
private val TagTextShadow = Shadow(color = Color.White.copy(alpha = 0.3f), offset = Offset(0f, 4f), blurRadius = 0f)

/** Tag shape: bevelled corners at the top, rounded at the bottom. */
private object GiftTagShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cut = with(density) { 44.dp.toPx() }
        val r = with(density) { 22.dp.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.22f, 0f)
            lineTo(w * 0.78f, 0f)
            lineTo(w, cut)
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r)
            lineTo(0f, cut)
            close()
        }
        return Outline.Generic(path)
    }
}

private val previewAssignment = DrawAssignmentUiModel(
    id = "a1",
    giverId = "p1",
    giverName = "Mara",
    receiverId = "p2",
    receiverName = "Jonas",
    wishes = listOf("Fantasy Bücher", "bunte Textmarker", "Schmuck"),
)

/**
 * Preview: closed gift tag ("Tap to open").
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagClosedPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment, onDismiss = {}, startOpened = false) }
}

/**
 * Preview: open gift tag with three wishes.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagOpenedPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment, onDismiss = {}, startOpened = true) }
}

/**
 * Preview: open gift tag without wishes (empty wishlist).
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagNoWishesPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment.copy(wishes = emptyList()), onDismiss = {}, startOpened = true) }
}

/**
 * Preview: open gift tag with a very long wish text.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagLongWishPreview() {
    WichtlyTheme {
        GiftTagReveal(
            assignment = previewAssignment.copy(
                wishes = listOf(
                    "Ein sehr ausführlicher und detaillierter Wunsch über mehrere Zeilen: Ein gebundenes Buch mit grünem Stoffeinband, mindestens 500 Seiten, aus dem Genre Epic Fantasy.",
                ),
            ),
            onDismiss = {},
            startOpened = true,
        )
    }
}
