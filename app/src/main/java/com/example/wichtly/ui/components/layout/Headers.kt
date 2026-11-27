package com.example.wichtly.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.controls.SectionActionButton
import com.example.wichtly.ui.components.display.SparkleStripWidth
import com.example.wichtly.ui.components.display.headerSparkles
import com.example.wichtly.ui.theme.EyebrowStyle
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Page header below the top bar: optional eyebrow, large serif title with a hard shadow,
 * optional subtitle and optional [meta] row. Gold sparkles are placed on the right.
 *
 * Without an eyebrow a spacer (14 dp) keeps about the same space free, so the title sits at
 * roughly the same height on every screen.
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    meta: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .headerSparkles(seed = title.hashCode())
            .padding(top = Spacing.inline, bottom = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                // shared eyebrow style (bold, 13sp), see EyebrowStyle in Type.kt
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Spacer(modifier = Modifier.height(Spacing.relaxed))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            // the strip on the right stays free for the sparkles (see headerSparkles)
            modifier = Modifier.padding(end = SparkleStripWidth),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        meta?.invoke()
    }
}

/** One entry of a [MetaRow]: an icon followed by a short text. */
data class MetaItem(val icon: ImageVector, val text: String)

/** Small row of icon-text pairs (e.g. date and draw type). Wraps instead of running over the edge. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetaRow(items: List<MetaItem>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.relaxed),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Section title with the number of entries as a gold badge right next to it.
 *
 * The top padding (12 dp) together with the item spacing of the LazyColumn
 * ([com.example.wichtly.ui.theme.Spacing.cardGap], 12 dp) gives the 24 dp gap between sections.
 *
 * @param action optional action on the right edge (e.g. [SectionActionButton] "Add"),
 *   so adding sits right next to the list it changes.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (count != null) {
            CountBadge(count = count)
        }
        if (action != null) {
            Spacer(modifier = Modifier.weight(1f))
            action()
        }
    }
}

@Composable
private fun CountBadge(count: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                .padding(horizontal = Spacing.tight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun HeadersPreview() {
    WichtlyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PageHeader(
                title = "Familie Wichteln",
                eyebrow = "Gruppe",
                meta = {
                    MetaRow(
                        listOf(
                            MetaItem(Icons.Filled.Event, "24.12.2026"),
                            MetaItem(Icons.Filled.Redeem, "Klassisch"),
                        )
                    )
                },
            )
            PageHeader(title = "Alle Gruppen", subtitle = "Gruppe antippen, dann wird gelost.")
            SectionHeader(title = "Teilnehmer", count = 6, action = { SectionActionButton(text = "Hinzufügen", onClick = {}) })
        }
    }
}
