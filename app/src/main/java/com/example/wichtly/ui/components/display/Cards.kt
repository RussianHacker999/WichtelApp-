package com.example.wichtly.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * The uniform list card of the app (participants, wishes, assignments of a draw, history,
 * preparation); groups have their own card (HeroGroupCard in GroupsScreen).
 *
 * Avatar on the left, title and subtitle, and a chip, button or ⋮ menu on the right.
 */
@Composable
fun ListItemCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    titleMaxLines: Int = 1,
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (selected) Modifier.border(BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary), shape) else Modifier)
    val body: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(start = Spacing.l, end = if (trailing != null) 6.dp else Spacing.l, top = Spacing.m, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            leading?.invoke()
            ListItemText(title = title, subtitle = subtitle, modifier = Modifier.weight(1f), titleMaxLines = titleMaxLines)
            if (trailing != null) {
                Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = cardModifier, shape = shape, colors = colors) { body() }
    } else {
        Card(modifier = cardModifier, shape = shape, colors = colors) { body() }
    }
}

/** Title and optional subtitle, as used in [ListItemCard] and the group card (HeroGroupCard). */
@Composable
fun ListItemText(title: String, subtitle: String?, modifier: Modifier = Modifier, titleMaxLines: Int = 1) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Arrow on the right edge for cards that open another screen. */
@Composable
fun ChevronIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.s),
    )
}

/** Small metric tile (e.g. "6 participants", "€25.00 budget per present"). */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Outlined hint card with an icon for short explanations, e.g. at the end of a screen. */
@Composable
fun NoteCard(text: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Outlined.Info) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            .padding(horizontal = Spacing.l, vertical = Spacing.relaxed),
        horizontalArrangement = Arrangement.spacedBy(Spacing.inline),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Card with rows of label and value (e.g. the overview before the draw). */
@Composable
fun InfoRowsCard(rows: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.inline)) {
            rows.forEach { (label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CardsPreview() {
    WichtlyTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ListItemCard(
                title = "Auslosung vom 01.12.2026",
                subtitle = "6 Teilnehmer · Klassisch",
                leading = { IconAvatar(icon = Icons.Filled.Redeem) },
                trailing = { StatusChip(text = "Gespeichert"); ChevronIcon() },
                onClick = {},
            )
            ListItemCard(title = "Mara", subtitle = "Wunschliste öffnen", leading = { InitialsAvatar(name = "Mara") }, selected = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(value = "6", label = "Teilnehmer", modifier = Modifier.weight(1f))
                StatTile(value = "25,00 €", label = "Budget pro Geschenk", modifier = Modifier.weight(1.7f))
            }
            InfoRowsCard(rows = listOf("Gruppe" to "Familie Wichteln", "Budget" to "25,00 €"))
            NoteCard(text = "Alte Ziehungen bleiben, wie sie waren.")
        }
    }
}
