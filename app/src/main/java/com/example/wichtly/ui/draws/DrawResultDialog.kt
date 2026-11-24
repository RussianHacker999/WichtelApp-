package com.example.wichtly.ui.draws

import com.example.wichtly.ui.theme.EyebrowStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wichtly.ui.components.Sparkle
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Vollbild-Dialog, der die gezogene Zuordnung (Schenker -> Beschenkter) als
 * Geschenkanhänger zeigt: erst geschlossen ("Tippen zum Öffnen"), nach dem Antippen geöffnet
 * inklusive der Wünsche des Beschenkten.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) nimmt die Komponente die fertigen Zuordnungsdaten entgegen
 * und löst über den Button "Schließen" lediglich [onDismiss] aus. Nur ob der Anhänger schon geöffnet
 * ist, merkt sich die Komponente selbst (rememberSaveable).
 *
 * @param assignment Die anzuzeigende Ziehungs-Zuordnung mit Schenker, Beschenktem und Wunschliste.
 * @param onDismiss Callback beim Schließen des Geschenkanhängers.
 * @param modifier Optionaler Layout-Modifier.
 * @param startOpened true öffnet den Anhänger sofort (z. B. für Previews).
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
            // Schnur des Anhängers
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
                            // Loch für die Schnur
                            drawCircle(color = scrim, radius = 8.dp.toPx(), center = Offset(size.width / 2f, 24.dp.toPx()))
                        }
                        .clickable(
                            enabled = !opened,
                            role = Role.Button,
                            onClickLabel = "Anhänger öffnen",
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { opened = true }
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 22.dp),
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
                            ClosedTag(giverName = assignment.giverName, contentColor = onTag)
                        }
                    }
                }
                Sparkle(size = 24.dp, rotationDegrees = 14f, modifier = Modifier.align(Alignment.TopStart).offset(x = (-30).dp, y = 64.dp))
                Sparkle(size = 11.dp, rotationDegrees = -20f, alpha = 0.75f, modifier = Modifier.align(Alignment.TopEnd).offset(x = 18.dp, y = 180.dp))
            }
        }
    }
}

@Composable
private fun ClosedTag(giverName: String, contentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Der geschlossene Anhänger ersetzt die frühere Sicherheitsabfrage ("Nur für deine Augen").
        TagLabel(text = "Nur für deine Augen", color = contentColor)
        Icon(imageVector = Icons.Filled.Redeem, contentDescription = null, tint = contentColor, modifier = Modifier.size(54.dp))
        Text(
            text = "$giverName, bist du das?",
            style = MaterialTheme.typography.headlineSmall.copy(shadow = TagTextShadow),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Bildschirm abschirmen und tippen zum Öffnen.",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpenedTag(assignment: DrawAssignmentUiModel, contentColor: Color, onDismiss: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TagLabel(text = "${assignment.giverName}, du beschenkst", color = contentColor)
        Text(
            text = assignment.receiverName,
            style = MaterialTheme.typography.displaySmall.copy(shadow = TagTextShadow),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Wunschliste von ${assignment.receiverName}",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
        )
        if (assignment.wishes.isEmpty()) {
            Text(
                text = "Zum Zeitpunkt der Auslosung waren keine Wünsche eingetragen.",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                assignment.wishes.forEach { wish ->
                    Surface(shape = MaterialTheme.shapes.small, color = Color.Black.copy(alpha = 0.14f), contentColor = contentColor) {
                        Text(
                            text = wish,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
            Text(text = "Schließen", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TagLabel(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        // Eyebrow auf dem Anhänger: fett, damit sie deutlich lesbar ist
        style = EyebrowStyle,
        color = color.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
    )
}

/** Heller Schatten für die Serifenschrift auf dem goldenen Anhänger (der dunkle Titelschatten wäre hier zu schwer). */
private val TagTextShadow = Shadow(color = Color.White.copy(alpha = 0.3f), offset = Offset(0f, 4f), blurRadius = 0f)

/** Anhänger-Form: oben abgeschrägte Ecken, unten abgerundet. */
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

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

private val previewAssignment = DrawAssignmentUiModel(
    giverId = "p1",
    giverName = "Mara",
    receiverId = "p2",
    receiverName = "Jonas",
    wishes = listOf("Fantasy Bücher", "bunte Textmarker", "Schmuck"),
)

/**
 * Preview 0: Geschlossener Geschenkanhänger ("Tippen zum Öffnen").
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagClosedPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment, onDismiss = {}, startOpened = false) }
}

/**
 * Preview 1: Geöffneter Geschenkanhänger mit drei Wünschen.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagOpenedPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment, onDismiss = {}, startOpened = true) }
}

/**
 * Preview 2: Geöffneter Geschenkanhänger ohne Wünsche (leere Wunschliste).
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22, heightDp = 640)
@Composable
private fun GiftTagNoWishesPreview() {
    WichtlyTheme { GiftTagReveal(assignment = previewAssignment.copy(wishes = emptyList()), onDismiss = {}, startOpened = true) }
}

/**
 * Preview 3: Geöffneter Geschenkanhänger mit einem sehr langen Wunschtext.
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
