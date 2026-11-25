package com.example.wichtly.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.display.Sparkle
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Das Grundgerüst aller Screens außer der Startseite (die ein eigenes Vollbild-Layout hat):
 * 1. Top-Leiste, 2. Seitenkopf und 3. Inhalt (beide in [content]), 4. optionale Aktionsleiste unten.
 */
@Composable
fun WichtlyScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        content = content,
    )
}

/**
 * Innenabstand für die scrollende Liste eines Screens: Abstände des Scaffolds (Top- und Aktionsleiste)
 * plus seitlicher Rand ([Spacing.screen]) und etwas Luft unten ([Spacing.xl]).
 */
fun screenContentPadding(innerPadding: PaddingValues): PaddingValues = PaddingValues(
    start = Spacing.screen,
    end = Spacing.screen,
    top = innerPadding.calculateTopPadding(),
    bottom = innerPadding.calculateBottomPadding() + Spacing.xl,
)

/**
 * Top-Leiste mit Zurück-Pfeil (wenn [onBack] gesetzt ist), einzeiligem Titel und optionalen Aktionen rechts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WichtlyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

/** Top-Leiste der Gruppenliste: Schriftzug „Wichtly“ mit Sternchen, ohne Zurück-Pfeil. (Die Startseite hat keine Top-Leiste.) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandTopBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(Spacing.xxs))
                Sparkle(size = 13.dp, rotationDegrees = 12f)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

/**
 * Feste Aktionsleiste am unteren Rand eines Screens. Darin entweder ein [SecondaryButton] und ein [PrimaryButton]
 * (beide mit weight 1f, damit keine Beschriftung abgeschnitten wird) oder ein einzelner [PrimaryButton] mit weight 1f.
 *
 * @param hint Optionale kurze Zeile über den Buttons, z. B. warum der Hauptbutton deaktiviert ist.
 */
@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.l, end = Spacing.l, top = Spacing.s),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Spacing.l, end = Spacing.l, top = Spacing.m, bottom = Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.inline),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Preview(showBackground = true, heightDp = 420)
@Composable
private fun WichtlyScaffoldPreview() {
    WichtlyTheme {
        WichtlyScaffold(
            topBar = { WichtlyTopBar(title = "Gruppendetails", onBack = {}) },
            bottomBar = {
                BottomActionBar(hint = "Mindestens 3 Teilnehmer zum Losen") {
                    PrimaryButton(text = "Losen", onClick = {}, icon = Icons.Filled.Redeem, enabled = false, modifier = Modifier.weight(1f))
                }
            },
        ) { innerPadding ->
            LazyColumn(contentPadding = screenContentPadding(innerPadding)) {
                item { PageHeader(title = "Familie Wichteln", eyebrow = "Gruppe") }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BrandTopBarPreview() {
    WichtlyTheme { BrandTopBar() }
}
