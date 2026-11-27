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
 * The basic frame of all screens except the start screen (which has its own full-screen layout):
 * 1. top bar, 2. page header and 3. content (both in [content]), 4. optional action bar at the bottom.
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
 * Content padding for the scrolling list of a screen: the scaffold insets (top and action bar)
 * plus the side margin ([Spacing.screen]) and some extra space at the bottom ([Spacing.xl]).
 */
fun screenContentPadding(innerPadding: PaddingValues): PaddingValues = PaddingValues(
    start = Spacing.screen,
    end = Spacing.screen,
    top = innerPadding.calculateTopPadding(),
    bottom = innerPadding.calculateBottomPadding() + Spacing.xl,
)

/**
 * Top bar with a back arrow (if [onBack] is set), a single-line title and optional actions on the right.
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

/** Top bar of the group list: the "Wichtly" wordmark with a sparkle, without a back arrow. (The start screen has no top bar.) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandTopBar(modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
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
 * Fixed action bar at the bottom of a screen. It holds either a [SecondaryButton] and a [PrimaryButton]
 * (both with weight 1f, so no label is cut off) or a single [PrimaryButton] with weight 1f.
 *
 * @param hint optional short line above the buttons, e.g. why the main button is disabled.
 */
@Composable
fun BottomActionBar(modifier: Modifier = Modifier, hint: String? = null, content: @Composable RowScope.() -> Unit) {
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
