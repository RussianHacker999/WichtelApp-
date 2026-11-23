package com.example.wichtly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.ui.groups.GroupsUiState
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoArea
import com.example.wichtly.ui.components.photo.PhotoThumbnail
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
import java.io.File
import java.time.LocalDate

// hier wurden die Schachteln onAddGroupClick, onGroupClick(groupId), onEditGroupClick(groupId) eingefügt
// die später in AppNavHost verdrahtet und dann jeweils zu "Formular neu", "Gruppendetail" und "Formular bearbeiten" geführt werden

@OptIn(ExperimentalMaterial3Api::class)
//um die UI-Elemente von Material 3 nutzen zu koennen
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
    onPhotoClick: (groupId: String) -> Unit,
    onPhotoFlowClose: () -> Unit,
    onPhotoCaptured: (groupId: String, File) -> Unit,
    onRemovePhoto: (groupId: String) -> Unit,
    onCameraDenied: (groupId: String) -> Unit,
) {
    Scaffold(
        //Scaffold quasi Geruest fuer einen Screen
        modifier = modifier,
        topBar = { TopAppBar(title = {Text("Alle Gruppen")}) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
            .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .weight(1f),
                    //(weight(1f):die LazyColum nimmt den gesamten Platz ein der zur Verfugung steht
                    //draengt den Losen Button nach ganz unten
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
                //jede Spalte muss in ein item (bei einer LazyColum)
                //ein Zeilen: item vs. mehere Zeilen: items
                item() {
                    Button(
                        onClick = onAddGroupClick,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text= "Gruppe hinzufügen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    if (uiState.isLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Zeigt einen Ladekreis (von Material 3)
                            CircularProgressIndicator()
                        }
                    } else if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                    }
                }
                items(uiState.groups) { group ->
                    //fuer jeden Namen in der names Liste wird ein Item mit einer Card erstellt
                    Card(
                        modifier = modifier.fillMaxWidth(),
                        // Hinterfrundfarbe fuer die Karten bestimmen
                        onClick = {onGroupClick(group.id)},
                        colors = CardDefaults.cardColors(
                            // ausgewaehlte Card ist bekommt einen Blauen Hintergrung, die anderen Karten einen hell grauen
                            containerColor = if (uiState.selectedGroupId == group.id) {
                                // Material 3 bietet verschiedene Frabrschemata an (Vorteil: automatische anpassung an hell und dunkelModus)
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }
                        )
                    ) {
                        //durch die Card wird z.B. die Graue Box eingefuegt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            //Button rechts
                            verticalAlignment = Alignment.CenterVertically
                            //Text mittig
                        ) {
                            // holte den Phad des Gruppenbildes aus dem UiSate
                            val photoFile = uiState.photoFiles[group.id]
                            IconButton(
                                // Click ruft dien CallBack onPhotoClick auf
                                // (oeffent ein Fenster, wo man ausesehlen kann, ob man ein Bild mit der Kamera aufnehmen will, das vorhandene Bild loeschen will oder zurueck zum GroupScreen gehen will
                                onClick = { onPhotoClick(group.id) },
                                modifier = Modifier.size(56.dp),
                            ) {
                                // wenn ein Pfad zum zum Gruppenbild dieser Gruppe hinterlegt ist,
                                // dann zeige das Bild an
                                if (photoFile != null) {
                                    // PhotoThumbnail ist ein Verkleinertes Bild des original Bildes
                                    // Ecken sind abgeschnitten
                                    PhotoThumbnail(
                                        // Pfad zum Foto
                                        file = photoFile,
                                        // Groesse des Fotos
                                        modifier = Modifier.size(44.dp),
                                        // Staerke des abgeschittenden Ecken (je hoeher die size, desto runder)
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                // Wenn kein Fototpfad hinterlegt wurde, wird ein Kamera-Icon angezeigt
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Gruppenfoto aufnehmen"
                                    )
                                }
                            }
                            Text(
                                text = "${group.name}",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .padding(
                                    start = 8.dp,
                                    top = 8.dp,
                                    end = 8.dp,
                                    bottom = 8.dp
                                    )
                                    .weight(1f)
                                    //weight(1f) sorgt dafuer, dass der Text den gesamten Platz einnimmt, der zur Verfuegung steht
                                //der Modifier sorgt fur den Abstand (nach oben, unten, links und rechts) zwischen dem Text und grauen Kasten
                            )
                            IconButton(
                                onClick = { onDetailsClick(group.id) },
                                modifier = modifier
                                    .padding(horizontal = 4.dp)
                                    //horiontal = absand nach links und nach rechts
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Gruppendetails"
                                )
                            }
                            IconButton(
                                onClick = {onEditGroupClick(group.id)},
                                modifier = modifier
                                    .padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Bearbeiten"
                                )
                            }
                        }
                    }
                }
            }
            Button(
                // Ohne ausgewählte Karte gibt es kein gültiges Ziel für die Vorbereitung.
                enabled = uiState.selectedGroupId != null,
                onClick = {
                    uiState.selectedGroupId?.let { selectedId ->
                        onPrepareDrawClick(selectedId)
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 16.dp)
            ) {
                Text(
                    text = "Losen",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // holt sich die Id des Fotos der Gruppe aus dem UiSate
            val activePhotoGroupId = uiState.photoFlowGroupId
            if (activePhotoGroupId != null) {
                val activeGroup = uiState.groups.firstOrNull { it.id == activePhotoGroupId }
                if (activeGroup != null) {
                    // oeffnet ein Composable das sich ueber den GroupSceen legt
                    // Abfrage, ob ein Foto aufgenommen werden soll, ein vorhandenes Foto geloescht werden soll oder das Comosable wieder geschlossen werden soll
                    PhotoCaptureFlow(
                        area = PhotoArea.GROUP,
                        hasPhoto = (activeGroup.photoPath != null),
                        isOpen = true,
                        onClose = onPhotoFlowClose,
                        onPhotoCaptured = { file ->
                            onPhotoCaptured(activePhotoGroupId, file)
                        },
                        onRemovePhoto = { onRemovePhoto(activePhotoGroupId) },
                        onCameraDenied = { onCameraDenied(activePhotoGroupId) },
                    )
                }
            }

            // oeffent Composable, wenn der Kamera Zugriff verweigert wurde
            if (uiState.cameraDeniedGroupId != null) {
                CameraPermissionDeniedHint(
                    onOpenSettings = {},
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

//Methode um den Screen als Vorschau anschauen zukoennen,
//ohne den Emulator und die App laufen zu lassen
//oeffen:   im Editor - da wo am die geoeffeneten Dateien wecheln kann
//          Code | Spilt | Design -> Waehle Split aus
@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    val testUiState = GroupsUiState(
        groups = listOf(
            GiftGroup(
                id = "1",
                name = "Familie",
                budgetCents = 60,
                drawDay = LocalDate.of(2025, 12,6)
            ),
            GiftGroup(
                id = "2",
                name = "Abteilung It",
                budgetCents = 1,
                drawDay = LocalDate.of(2026, 10, 15)
            ),
            GiftGroup(
                id = "3",
                name = "Uni",
                budgetCents = 20,
                drawDay = LocalDate.of(2026, 11, 4)
            ),
            GiftGroup(
                id = "4",
                name = "Familie",
                budgetCents = 60,
                drawDay = LocalDate.of(2026, 11, 6)
            ),
            GiftGroup(
                id = "5",
                name = "Buchclub",
                budgetCents = 20,
                drawDay = LocalDate.of(2026, 11, 8)
            ),
            GiftGroup(
                id = "6",
                name = "Volleyball",
                budgetCents = 15,
                drawDay = LocalDate.now()
            )
        )
    )
    WichtlyTheme() {
        GroupsScreen(
            testUiState,
            onAddGroupClick = {},
            onGroupClick = {},
            onDetailsClick = {},
            onEditGroupClick = {},
            onPrepareDrawClick = {},
            onPhotoClick = {},
            onPhotoFlowClose = {},
            onPhotoCaptured = { _, _ -> },
            onRemovePhoto = {},
            onCameraDenied = {},
        )
    }
}
