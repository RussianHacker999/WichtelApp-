package com.example.wichtly.ui.start.screen

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.controls.WichtlyTextButton
import com.example.wichtly.ui.components.display.Sparkle
import com.example.wichtly.ui.theme.EyebrowStyle
import com.example.wichtly.ui.theme.OnGoldContainer
import com.example.wichtly.ui.theme.Pine700
import com.example.wichtly.ui.theme.Pine900
import com.example.wichtly.ui.theme.Pine950
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import kotlin.random.Random

// Fixed dimensions of the start image: space above the wordmark and indent of the footer
private val HeroTopSpace = 140.dp
private val HeroSidePadding = 28.dp

/**
 * Start screen (mockup option A "night sky"): the large Wichtly wordmark centred on a glowing
 * night sky with snow, below it an info line and a main action.
 *
 * The large wordmark exists only here; the group list keeps its small brand bar (BrandTopBar).
 *
 * Stateless: [summary] comes from the route, actions go through callbacks.
 *
 * @param summary info line; null while the groups are still loading (the footer then stays empty).
 * @param onOpenGroups opens the group list.
 * @param onCreateGroup opens the form for a new group.
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
                // Glow from the top centre, getting darker towards the bottom
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
            // 1. Centre: kicker, wordmark, claim
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
            // 2. Footer: info line + actions (empty while loading)
            if (summary != null) {
                StartFooter(summary = summary, onOpenGroups = onOpenGroups, onCreateGroup = onCreateGroup)
            } else {
                Spacer(modifier = Modifier.height(HeroTopSpace))
            }
        }
    }
}

/** Darkening towards the bottom edge, so the buttons sit on a calm background. */
private val NightSkyBrush = Brush.verticalGradient(
    0f to Color.Transparent,
    1f to Pine950.copy(alpha = 0.6f),
)

/**
 * The large wordmark. It must not be cut off:
 * - autoSize shrinks the font step by step (72sp → 36sp) until "Wichtly" fits in one line,
 *   also on narrow devices or with a large system font.
 * - The sparkles sit in their own reserved margin at the top right (padding), not over the text,
 *   so they are neither cut off nor push the text aside.
 * - lineHeight > fontSize, so ascenders and descenders (W, y) have enough room.
 */
@Composable
private fun Wordmark() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.padding(start = HeroSidePadding, end = HeroSidePadding, top = Spacing.relaxed)) {
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
            // Sparkle in the reserved 28dp margin at the top right (outside the text area)
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

/** Subtle info line above the buttons, e.g. "Next exchange · in 30 days". */
@Composable
private fun InfoPill(summary: StartSummary) {
    val gold = MaterialTheme.colorScheme.primary
    // Resolve texts first: buildAnnotatedString is not a composable context.
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
            modifier = Modifier.padding(horizontal = Spacing.relaxed, vertical = Spacing.s),
        )
    }
}

/** "today", "tomorrow" or "in N days" (texts and plural in strings.xml). */
@Composable
private fun daysUntilText(days: Long): String =
    daysUntilSpecialRes(days)?.let { stringResource(it) }
        ?: pluralStringResource(R.plurals.start_days_in, days.toInt(), days)

/** Still snow: 70 fixed points spread by a seed over the upper 80 % of the area (no animation). */
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

/** Preview: start screen with an upcoming exchange. */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenWithGroupsPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NextEvent("Familie Wichteln", 30), onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview: start screen without a group. */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenEmptyPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NoGroups, onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview: narrow device with a large system font; the wordmark must not be cut off. */
@Preview(showBackground = true, widthDp = 300, heightDp = 620, fontScale = 1.5f)
@Composable
private fun StartScreenNarrowLargeFontPreview() {
    WichtlyTheme {
        StartScreen(summary = StartSummary.NextEvent("Büro Wichteln 2026", 1), onOpenGroups = {}, onCreateGroup = {})
    }
}

/** Preview: loading state (footer still empty). */
@Preview(showBackground = true, heightDp = 760)
@Composable
private fun StartScreenLoadingPreview() {
    WichtlyTheme {
        StartScreen(summary = null, onOpenGroups = {}, onCreateGroup = {})
    }
}
