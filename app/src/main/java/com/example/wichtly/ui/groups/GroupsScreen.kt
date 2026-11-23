package com.example.wichtly.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.ui.components.BottomActionBar
import com.example.wichtly.ui.components.BrandTopBar
import com.example.wichtly.ui.components.ChipTone
import com.example.wichtly.ui.components.ItemMenu
import com.example.wichtly.ui.components.ListItemText
import com.example.wichtly.ui.components.MenuAction
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.PrimaryButton
import com.example.wichtly.ui.components.SecondaryButton
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.StatusChip
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.areaSparkles
import com.example.wichtly.ui.components.initialsOf
import com.example.wichtly.ui.components.photo.PhotoThumbnail
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.groups.GroupsUiState
import com.example.wichtly.ui.groups.form.formatCentsForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Startscreen mit allen Wichtelgruppen (#1).
 *
 * Antippen einer Karte wählt die Gruppe aus, "Losen" in der Aktionsleiste öffnet dann die
 * Vorbereitung. Details und Bearbeiten liegen im ⋮-Menü der Karte. Das Gruppenfoto wird nur
 * noch im Formular gesetzt und hier als Kartenbild gezeigt.
 *
 * @param uiState Gruppen, Fotos und die ausgewählte Gruppe.
 * @param onAddGroupClick Öffnet das Formular für eine neue Gruppe.
 * @param onGroupClick Markiert eine Gruppe für "Losen".
 * @param onDetailsClick Öffnet die Gruppendetails.
 * @param onEditGroupClick Öffnet das Formular zum Bearbeiten.
 * @param onPrepareDrawClick Öffnet die Vorbereitung der Auslosung für die ausgewählte Gruppe.
 */
@Composable
fun GroupsScreen(
    uiState: GroupsUiState,
    modifier: Modifier = Modifier,
    onAddGroupClick: () -> Unit,
    onGroupClick: (groupId: String) -> Unit,
    onDetailsClick: (groupId: String) -> Unit,
    onEditGroupClick: (groupId: String) -> Unit,
    onPrepareDrawClick: (groupId: String) -> Unit,
) {
    WichtlyScaffold(
        modifier = modifier,
        topBar = { BrandTopBar() },
        bottomBar = {
            BottomActionBar {
                SecondaryButton(
                    text = "Gruppe",
                    onClick = onAddGroupClick,
                    icon = Icons.Filled.Add,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = "Losen",
                    // Ohne ausgewählte Karte gibt es kein gültiges Ziel für die Vorbereitung.
                    enabled = uiState.selectedGroupId != null,
                    onClick = { uiState.selectedGroupId?.let(onPrepareDrawClick) },
                    icon = Icons.Filled.Redeem,
                    modifier = Modifier.weight(1.4f),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = screenContentPadding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
        ) {
            item {
                PageHeader(title = "Alle Gruppen", subtitle = "Gruppe antippen, dann wird gelost.")
            }
            if (uiState.groups.isEmpty()) {
                item {
                    MessageState(
                        title = "Noch ganz schön leer hier",
                        text = "Lege deine erste Gruppe an, dann kann der Spaß beginnen.",
                        icon = Icons.Filled.Redeem,
                        fillMaxSize = false,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                    )
                }
            } else {
                item { SectionHeader(title = "Gruppen", count = uiState.groups.size) }
                items(uiState.groups, key = { it.id }) { group ->
                    HeroGroupCard(
                        group = group,
                        photoFile = uiState.photoFiles[group.id],
                        selected = uiState.selectedGroupId == group.id,
                        onClick = { onGroupClick(group.id) },
                        onDetailsClick = { onDetailsClick(group.id) },
                        onEditClick = { onEditGroupClick(group.id) },
                    )
                }
            }
        }
    }
}

private val chipDateFormatter = DateTimeFormatter.ofPattern("dd.MM.")

/**
 * Gruppenkarte mit farbigem Bildbereich (Gruppenfoto oder Verlauf mit Sternchen),
 * darunter Name, Budget und Wichtelart sowie das ⋮-Menü.
 */
@Composable
private fun HeroGroupCard(
    group: GiftGroup,
    photoFile: File?,
    selected: Boolean,
    onClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.border(BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary), shape) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
        ) {
            if (photoFile != null) {
                PhotoThumbnail(file = photoFile, modifier = Modifier.fillMaxSize(), shape = RectangleShape)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
                            )
                        )
                        .areaSparkles(seed = group.id.hashCode()),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = Spacing.l, bottom = Spacing.m)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = initialsOf(group.name), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.inverseOnSurface)
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 66.dp, y = (-12).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Ausgewählt",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            StatusChip(
                text = group.drawDay.format(chipDateFormatter),
                tone = ChipTone.OnImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.m),
            )
        }
        Row(
            modifier = Modifier.padding(start = Spacing.l, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItemText(
                title = group.name,
                subtitle = "${formatCentsForInput(group.budgetCents)} € · ${group.drawType.displayName}",
                modifier = Modifier.weight(1f),
            )
            ItemMenu(
                actions = listOf(
                    MenuAction("Gruppendetails", Icons.Filled.Groups, onDetailsClick),
                    MenuAction("Gruppe bearbeiten", Icons.Filled.Edit, onEditClick),
                ),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun GroupsScreenPreview() {
    val groups = listOf(
        GiftGroup(id = "1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24)),
        GiftGroup(id = "2", name = "Büro Wichteln 2026", budgetCents = 1500, drawDay = LocalDate.of(2026, 12, 15), drawType = DrawType.JUNK),
        GiftGroup(id = "3", name = "Buchclub", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 8)),
    )
    WichtlyTheme {
        GroupsScreen(
            uiState = GroupsUiState(groups = groups, selectedGroupId = "1"),
            onAddGroupClick = {},
            onGroupClick = {},
            onDetailsClick = {},
            onEditGroupClick = {},
            onPrepareDrawClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun GroupsScreenEmptyPreview() {
    WichtlyTheme {
        GroupsScreen(
            uiState = GroupsUiState(),
            onAddGroupClick = {},
            onGroupClick = {},
            onDetailsClick = {},
            onEditGroupClick = {},
            onPrepareDrawClick = {},
        )
    }
}
