package com.example.wichtly.ui.groups.list.screen

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import com.example.wichtly.ui.components.display.labelRes
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
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
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.ui.components.layout.BottomActionBar
import com.example.wichtly.ui.components.layout.BrandTopBar
import com.example.wichtly.ui.components.display.ChipTone
import com.example.wichtly.ui.components.controls.ItemMenu
import com.example.wichtly.ui.components.display.ListItemText
import com.example.wichtly.ui.components.controls.MenuAction
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.controls.PrimaryButton
import com.example.wichtly.ui.components.controls.SectionActionButton
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.display.StatusChip
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.display.areaSparkles
import com.example.wichtly.ui.components.display.initialsOf
import com.example.wichtly.ui.components.photo.PhotoThumbnail
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.components.display.formatCentsForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// hier wurden die Schachteln onAddGroupClick, onGroupClick(groupId), onEditGroupClick(groupId) eingefügt
// die später in AppNavHost verdrahtet und dann jeweils zu "Formular neu", "Gruppendetail" und "Formular bearbeiten" geführt werden

// Startscreen mit allen Gruppen; die Bausteine (Top-Leiste, Karten, Buttons) kommen aus ui/components
@Composable
//die Annotation bescheibt, dass die Fkt. UI-Elemente bescheibt - frueher Arbeit mit XML
fun GroupsScreen(
    uiState: GroupsUiState,
    modifier: Modifier = Modifier,
    // Callbacks/Event-Handler (Funktionen die von aussen in das Composable reingereicht werden)
    onAddGroupClick: () -> Unit,
    onGroupClick: (groupId: String) -> Unit,
    onDetailsClick: (groupId: String) -> Unit,
    onEditGroupClick: (groupId: String) -> Unit,
    onPrepareDrawClick: (groupId: String) -> Unit,
) {
    WichtlyScaffold(
        // WichtlyScaffold ist das gemeinsame Geruest jedes Screens (Top-Leiste, Inhalt, Aktionsleiste unten)
        modifier = modifier,
        topBar = { BrandTopBar() },
        bottomBar = {
            // "Losen" ist die EINE Hauptaktion; "Neue Gruppe" sitzt am Abschnitt "Gruppen".
            // Ohne Gruppen (oder beim Laden/Fehler) gibt es nichts zu losen, also keine Aktionsleiste.
            if (!uiState.isLoading && uiState.errorMessage == null && uiState.groups.isNotEmpty()) {
                val hasSelection = uiState.selectedGroupId != null
                BottomActionBar(hint = if (hasSelection) null else stringResource(R.string.groups_select_hint)) {
                    PrimaryButton(
                        text = stringResource(R.string.common_draw),
                        // Ohne ausgewählte Karte gibt es kein gültiges Ziel für die Vorbereitung.
                        enabled = hasSelection,
                        onClick = { uiState.selectedGroupId?.let(onPrepareDrawClick) },
                        icon = Icons.Filled.Redeem,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // contentPadding: Abstand zum Bildschirmrand und zu Top- und Aktionsleiste
            // der Losen-Button steht nicht in der Liste, sondern fest in der Aktionsleiste unten
            contentPadding = screenContentPadding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
        ) {
            item {
                // jedes Element muss in ein item (bei einer LazyColumn)
                // ein Element: item vs. mehrere Elemente aus einer Liste: items
                PageHeader(title = stringResource(R.string.groups_title), subtitle = stringResource(R.string.groups_subtitle))
            }
            if (uiState.isLoading) {
                item {
                    // Zeigt einen Ladekreis (von Material 3)
                    LoadingState(
                        text = stringResource(R.string.common_loading_unpacking),
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                    )
                }
            } else if (uiState.errorMessage != null) {
                item {
                    MessageState(
                        title = stringResource(R.string.common_error_loading_title),
                        text = stringResource(uiState.errorMessage),
                        icon = Icons.Outlined.ErrorOutline,
                        isError = true,
                        fillMaxSize = false,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                    )
                }
            } else if (uiState.groups.isEmpty()) {
                item {
                    MessageState(
                        title = stringResource(R.string.groups_empty_title),
                        text = stringResource(R.string.groups_empty_text),
                        icon = Icons.Filled.Redeem,
                        actionLabel = stringResource(R.string.action_create_first_group),
                        onAction = onAddGroupClick,
                        fillMaxSize = false,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                    )
                }
            } else {
                item {
                    SectionHeader(
                        title = stringResource(R.string.groups_section),
                        count = uiState.groups.size,
                        action = { SectionActionButton(text = stringResource(R.string.groups_new), onClick = onAddGroupClick) },
                    )
                }
                items(uiState.groups, key = { it.id }) { group ->
                    // fuer jede Gruppe in uiState.groups wird eine HeroGroupCard erstellt
                    HeroGroupCard(
                        group = group,
                        // holt den Pfad des Gruppenbildes aus dem UiState (null, wenn kein Foto hinterlegt ist)
                        photoFile = uiState.photoFiles[group.id],
                        // die ausgewaehlte Karte bekommt einen goldenen Rahmen und ein Haekchen
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

// Das Gruppenfoto wird nicht mehr in der Liste aufgenommen. Auswahl (Foto aufnehmen / entfernen),
// Kamera-Abfrage und der Hinweis bei abgelehntem Kamerazugriff laufen jetzt im Gruppenformular
// (GroupFormScreen) ueber PhotoCaptureFlow, genau wie beim Teilnehmerfoto.

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
        // Hintergrundfarbe fuer die Karten bestimmen
        // Material 3 bietet verschiedene Frabrschemata an (Vorteil: automatische anpassung an hell und dunkelModus)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
        ) {
            // wenn ein Pfad zum zum Gruppenbild dieser Gruppe hinterlegt ist,
            // dann zeige das Bild an
            if (photoFile != null) {
                // PhotoThumbnail ist ein verkleinertes Bild des Originalbildes
                // es fuellt den oberen Bildbereich der Karte komplett aus
                PhotoThumbnail(
                    // Pfad zum Foto
                    file = photoFile,
                    // Groesse des Fotos: ganzer Bildbereich
                    modifier = Modifier.fillMaxSize(),
                    // Form des Fotos: eckig, die runden Ecken entstehen durch die Karte
                    shape = RectangleShape,
                )
            // Wenn kein Fotopfad hinterlegt wurde, werden ein Farbverlauf mit Sternchen und die Initialen angezeigt
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
                        contentDescription = stringResource(R.string.common_selected),
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
            // untere Zeile der Karte: Name, Budget und Wichtelart, rechts das ⋮-Menü
            modifier = Modifier.padding(start = Spacing.l, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            // Text und Menü vertikal mittig
        ) {
            ListItemText(
                title = group.name,
                subtitle = stringResource(R.string.groups_card_subtitle, formatCentsForInput(group.budgetCents), stringResource(group.drawType.labelRes())),
                //weight(1f) sorgt dafuer, dass der Text den gesamten Platz einnimmt, der zur Verfuegung steht
                // den Abstand zum Kartenrand gibt das padding der Row
                modifier = Modifier.weight(1f),
            )
            // ⋮-Menü rechts mit "Gruppendetails" und "Gruppe bearbeiten"
            ItemMenu(
                actions = listOf(
                    MenuAction(stringResource(R.string.common_group_details), Icons.Filled.Groups, onDetailsClick),
                    MenuAction(stringResource(R.string.common_edit_group), Icons.Filled.Edit, onEditClick),
                ),
            )
        }
    }
}

//Methode um den Screen als Vorschau anschauen zukoennen,
//ohne den Emulator und die App laufen zu lassen
//oeffen:   im Editor - da wo am die geoeffeneten Dateien wecheln kann
//          Code | Spilt | Design -> Waehle Split aus
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
