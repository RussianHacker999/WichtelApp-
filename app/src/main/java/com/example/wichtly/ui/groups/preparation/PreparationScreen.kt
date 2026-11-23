package com.example.wichtly.ui.groups.preparation

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

/**
 * Screen zur Kontrolle vor dem Start der Auslosung (#4).
 *
 * @param state Gruppe, Teilnehmer und ob gelost werden darf.
 * @param onBack Zurück zur vorherigen Ansicht.
 * @param onStartDraw Startet die Auslosung.
 */
@Composable
fun PreparationScreen(
    state: PreparationUiState,
    onBack: () -> Unit,
    onStartDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = state.group
    val showContent = !state.isLoading && state.errorMessage == null && !state.notFound && group != null
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = "Vorbereitung", onBack = onBack) },
        bottomBar = {
            if (showContent) {
                BottomActionBar {
                    SecondaryButton(text = "Zurück", onClick = onBack, modifier = Modifier.weight(1f))
                    PrimaryButton(
                        text = if (state.isStarting) "Wird ausgelost..." else "Jetzt losen",
                        onClick = onStartDraw,
                        icon = Icons.Filled.Redeem,
                        // Losen ist nur möglich, wenn alle Voraussetzungen erfüllt sind.
                        enabled = state.canStartDraw,
                        loading = state.isStarting,
                        modifier = Modifier.weight(1.4f),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(text = "Einen Moment, wird ausgepackt …")
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            state.notFound || group == null -> MessageState(
                title = "Diese Gruppe ist verschwunden",
                text = "Die ausgewählte Gruppe wurde nicht in der Datenbank gefunden.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    PageHeader(
                        eyebrow = "Fast geschafft",
                        title = "Alles bereit?",
                        subtitle = "Bitte überprüfe, ob die Daten der Gruppe korrekt sind, bevor du die Auslosung startest.",
                    )
                }
                item {
                    InfoRowsCard(
                        rows = listOf(
                            "Gruppe" to group.name,
                            "Art des Wichtelns" to group.drawType.displayName,
                            "Budget" to "${formatCentsForInput(group.budgetCents)} €",
                            "Übergabedatum" to formatDateForInput(group.drawDay),
                            "Teilnehmer" to state.participants.size.toString(),
                        )
                    )
                }
                item {
                    if (state.canStartDraw) {
                        StatusLine(
                            icon = Icons.Filled.CheckCircle,
                            text = "Alles startklar, es kann gelost werden!",
                            color = com.example.wichtly.ui.theme.WichtlyTheme.extendedColors.success,
                        )
                    } else {
                        StatusLine(
                            icon = Icons.Outlined.ErrorOutline,
                            text = "Mindestens drei Teilnehmer werden benötigt.",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                item { SectionHeader(title = "Teilnehmer", count = state.participants.size) }
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
            state = PreparationUiState(isLoading = false, group = previewGroup, participants = previewParticipants, canStartDraw = true),
            onBack = {}, onStartDraw = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreparationScreenTooFewPreview() {
    WichtlyTheme {
        PreparationScreen(
            state = PreparationUiState(isLoading = false, group = previewGroup, participants = previewParticipants.take(2), canStartDraw = false),
            onBack = {}, onStartDraw = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenLoadingPreview() {
    WichtlyTheme {
        PreparationScreen(state = PreparationUiState(isLoading = true), onBack = {}, onStartDraw = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenNotFoundPreview() {
    WichtlyTheme {
        PreparationScreen(state = PreparationUiState(isLoading = false, notFound = true), onBack = {}, onStartDraw = {})
    }
}
