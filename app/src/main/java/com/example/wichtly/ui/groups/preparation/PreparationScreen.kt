package com.example.wichtly.ui.groups.preparation

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import com.example.wichtly.ui.components.labelRes
import com.example.wichtly.domain.model.DrawRules
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.InfoRowsCard
import com.example.wichtly.ui.components.InitialsAvatar
import com.example.wichtly.ui.components.ListItemCard
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SecondaryButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.groups.form.formatCentsForInput
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/** Screen zur Kontrolle vor dem Start der Auslosung */
@Composable
fun PreparationScreen(
    state: PreparationUiState,
    // Methode fuer "Zurück" (Pfeil in der Top-Leiste und Button in der Aktionsleiste)
    onBack: () -> Unit,
    // Methode fuer Button "jetzt losen"
    onStartDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = state.group
    // Solange weder Gruppe noch Fehler noch "nicht gefunden" vorliegen, wird noch geladen.
    val isLoading = group == null && state.errorMessage == null && !state.notFound
    val showContent = state.errorMessage == null && !state.notFound && group != null
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = stringResource(R.string.preparation_title), onBack = onBack) },
        bottomBar = {
            if (showContent) {
                // Die unteren Aktionen bleiben beim Scrollen sichtbar.
                BottomActionBar {
                    SecondaryButton(text = stringResource(R.string.common_back), onClick = onBack, modifier = Modifier.weight(1f))
                    // Der goldene Button "Jetzt losen" ist immer sichtbar, aber nur aktiv, wenn alle Voraussetzungen erfüllt sind.
                    PrimaryButton(
                        text = stringResource(if (state.isStarting) R.string.preparation_drawing else R.string.preparation_draw_now),
                        onClick = onStartDraw,
                        icon = Icons.Filled.Redeem,
                        enabled = state.canStartDraw,
                        loading = state.isStarting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            isLoading -> LoadingState(text = stringResource(R.string.common_loading_unpacking))
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back),
                onAction = onBack,
            )
            state.notFound || group == null -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.preparation_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back),
                onAction = onBack,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    PageHeader(
                        eyebrow = stringResource(R.string.preparation_eyebrow),
                        title = stringResource(R.string.preparation_header),
                        subtitle = stringResource(R.string.preparation_subtitle),
                    )
                }
                item {
                    InfoRowsCard(
                        rows = listOf(
                            stringResource(R.string.common_group) to group.name,
                            stringResource(R.string.preparation_row_draw_type) to stringResource(group.drawType.labelRes()),
                            stringResource(R.string.preparation_row_budget) to stringResource(R.string.common_amount_euro, formatCentsForInput(group.budgetCents)),
                            stringResource(R.string.preparation_row_date) to formatDateForInput(group.drawDay),
                            stringResource(R.string.common_participants) to state.participants.size.toString(),
                        )
                    )
                }
                item {
                    if (state.canStartDraw) {
                        StatusLine(
                            icon = Icons.Filled.CheckCircle,
                            text = stringResource(R.string.preparation_ready),
                            color = com.example.wichtly.ui.theme.WichtlyTheme.extendedColors.success,
                        )
                    } else {
                        StatusLine(
                            icon = Icons.Outlined.ErrorOutline,
                            text = stringResource(R.string.preparation_min_participants, DrawRules.MIN_PARTICIPANTS),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                item { SectionHeader(title = stringResource(R.string.common_participants), count = state.participants.size) }
                // Die Liste kommt über das PreparationViewModel aus dem ParticipantRepository.
                items(state.participants, key = { it.id }) { participant ->
                    ListItemCard(
                        title = participant.name,
                        leading = { InitialsAvatar(name = participant.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLine(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = text, style = MaterialTheme.typography.titleSmall, color = color)
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

private val previewGroup = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24), drawType = DrawType.NORMAL)
private val previewParticipants = listOf("Mara", "Jonas", "Leonie", "Emil").mapIndexed { i, n -> Participant(id = "p$i", groupId = "g1", name = n) }

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun PreparationScreenReadyPreview() {
    WichtlyTheme {
        PreparationScreen(
            state = PreparationUiState(group = previewGroup, participants = previewParticipants, canStartDraw = true),
            onBack = {}, onStartDraw = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreparationScreenTooFewPreview() {
    WichtlyTheme {
        PreparationScreen(
            state = PreparationUiState(group = previewGroup, participants = previewParticipants.take(2), canStartDraw = false),
            onBack = {}, onStartDraw = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenLoadingPreview() {
    WichtlyTheme {
        PreparationScreen(state = PreparationUiState(), onBack = {}, onStartDraw = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenNotFoundPreview() {
    WichtlyTheme {
        PreparationScreen(state = PreparationUiState(notFound = true), onBack = {}, onStartDraw = {})
    }
}
