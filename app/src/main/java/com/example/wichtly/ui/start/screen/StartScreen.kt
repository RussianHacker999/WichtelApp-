package com.example.wichtly.ui.start.screen

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.wichtly.R
import com.example.wichtly.ui.theme.EyebrowStyle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.display.Sparkle
import com.example.wichtly.ui.components.controls.WichtlyTextButton
import com.example.wichtly.ui.theme.OnGoldContainer
import com.example.wichtly.ui.theme.Pine700
import com.example.wichtly.ui.theme.Pine900
import com.example.wichtly.ui.theme.Pine950
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import kotlin.random.Random

/**
 * Startseite (Mockup-Option A "Nachthimmel"): großer Wichtly-Schriftzug mittig auf einem
 * leuchtenden Nachthimmel mit Schnee, darunter eine Infozeile und eine Hauptaktion.
 *
 * Der große Schriftzug gibt es NUR hier; die Gruppenliste behält ihre kleine Marken-Leiste.
 *
 * Zustandslos: [summary] kommt aus der Route, Aktionen laufen über Callbacks.
 *
 * @param summary Infozeile; null, solange die Gruppen noch geladen werden (dann bleibt der Fuß leer).
 * @param onOpenGroups Öffnet die Gruppenliste.
 * @param onCreateGroup Öffnet das Formular für eine neue Gruppe.
 */
@Composable
fun StartScreen(
    summary: StartSummary?,
    onOpenGroups: () -> Unit,
    onCreateGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Leuchten von oben in der Mitte, nach unten dunkler werdend
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Pine700, Pine900, Pine950),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.maxDimension * 0.85f,
                    )
                )
                drawRect(NightSkyBrush)
            },
    ) {
        SnowLayer(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Mitte: Kicker, Schriftzug, Claim
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.m, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(R.string.start_kicker),
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Wordmark()
                Text(
                    text = stringResource(R.string.start_claim),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 260.dp),
                )
            }
            // 2. Fuß: Infozeile + Aktionen (leer, solange geladen wird)
            if (summary != null) {
                StartFooter(summary = summary, onOpenGroups = onOpenGroups, onCreateGroup = onCreateGroup)
            } else {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
}

/** Abdunklung zum unteren Rand, damit die Buttons ruhig stehen. */
private val NightSkyBrush = Brush.verticalGradient(
    0f to Color.Transparent,
    1f to Pine950.copy(alpha = 0.6f),
)

/**
 * Der große Schriftzug. Er darf NIE abgeschnitten werden:
 * - autoSize verkleinert die Schrift schrittweise (72sp → 36sp), bis "Wichtly" in eine Zeile passt,
 *   auch bei schmalen Geräten oder großer Systemschrift.
 * - Die Sternchen sitzen in einem eigenen, reservierten Rand rechts/oben (padding), nicht über dem Text,
 *   damit sie weder abgeschnitten werden noch den Text verdrängen.
 * - lineHeight > fontSize, damit Ober- und Unterlängen (W, y) vollständig Platz haben.
 */
@Composable
private fun Wordmark() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 14.dp)) {
            BasicText(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.25.em,
                ),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 36.sp, maxFontSize = 72.sp, stepSize = 2.sp),
            )
            // Sternchen im reservierten 28dp-Rand rechts oben (außerhalb der Textfläche)
            Sparkle(
                size = 24.dp,
                rotationDegrees = 12f,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 26.dp, y = (-12).dp),
            )
            Sparkle(
                size = 10.dp,
                rotationDegrees = -18f,
                alpha = 0.75f,
                color = OnGoldContainer,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 26.dp, y = 18.dp),
            )
        }
    }
}

@Composable
private fun StartFooter(summary: StartSummary, onOpenGroups: () -> Unit, onCreateGroup: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        InfoPill(summary)
        Spacer(modifier = Modifier.height(Spacing.xs))
        if (summary is StartSummary.NoGroups) {
            PrimaryButton(
                text = stringResource(R.string.action_create_first_group),
                onClick = onCreateGroup,
                icon = Icons.Filled.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            PrimaryButton(
                text = stringResource(R.string.start_to_groups),
                onClick = onOpenGroups,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
            )
            WichtlyTextButton(text = stringResource(R.string.action_create_group), onClick = onCreateGroup)
        }
    }
}

/** Dezente Infozeile über den Buttons, z. B. "Nächste Bescherung · in 30 Tagen". */
@Composable
private fun InfoPill(summary: StartSummary) {
    val gold = MaterialTheme.colorScheme.primary
    // Texte vorab auflösen: buildAnnotatedString ist kein Composable-Kontext.
    val separator = stringResource(R.string.start_info_separator)
    val (prefix, highlight) = when (summary) {
        StartSummary.NoGroups ->
            stringResource(R.string.start_info_no_groups) to stringResource(R.string.start_info_no_groups_highlight)
        is StartSummary.NextEvent ->
            summary.groupName to daysUntilText(summary.daysUntil)
        StartSummary.Unavailable ->
            stringResource(R.string.start_info_unavailable) to null
        is StartSummary.OnlyPast ->
            pluralStringResource(R.plurals.start_info_group_count, summary.groupCount, summary.groupCount) to
                stringResource(R.string.start_info_next_round)
    }
    val text = buildAnnotatedString {
        append(prefix)
        if (highlight != null) {
            append(separator)
            withStyle(SpanStyle(color = gold, fontWeight = FontWeight.Bold)) { append(highlight) }
        }
    }
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

/** "heute", "morgen" oder "in N Tagen" (Texte und Plural in strings.xml). */
@Composable
private fun daysUntilText(days: Long): String =
    daysUntilSpecialRes(days)?.let { stringResource(it) }
        ?: pluralStringResource(R.plurals.start_days_in, days.toInt(), days)

/** Ruhender Schnee: feste, per Seed verteilte Punkte im oberen Bereich (keine Animation). */
@Composable
private fun SnowLayer(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = Random(42)
        repeat(70) {
            drawCircle(
                color = Color.White.copy(alpha = 0.10f + r.nextFloat() * 0.35f),
                radius = (0.6f + r.nextFloat() * 1.6f).dp.toPx(),
                center = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height * 0.8f),
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/** Preview 1: Startseite mit anstehender Bescherung. */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenWithGroupsPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NextEvent("Familie Wichteln", 30), onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview 2: Startseite ohne Gruppe. */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenEmptyPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NoGroups, onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview 3: Schmales Gerät mit großer Systemschrift: der Schriftzug darf nicht abgeschnitten werden. */
@Preview(showBackground = true, widthDp = 300, heightDp = 620, fontScale = 1.5f)
@Composable
private fun StartScreenNarrowLargeFontPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NextEvent("Büro Wichteln 2026", 1), onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview 4: Ladezustand (Fuß noch leer). */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenLoadingPreview() {
    WichtlyTheme {
        StartScreen(summary = null, onOpenGroups = {}, onCreateGroup = {})
    }
}
