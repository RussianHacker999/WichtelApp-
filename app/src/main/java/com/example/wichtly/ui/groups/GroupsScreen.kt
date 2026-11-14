package com.example.wichtly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.ui.groups.GroupsUiState
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
//um die UI-Elemente von Material 3 nutzen zu koennen
@Composable
//die Annotation bescheibt, dass die Fkt. UI-Elemente bescheibt - frueher Arbeit mit XML
fun GroupsScreen(uiState: GroupsUiState, modifier: Modifier = Modifier){
    Scaffold(
        //Scaffold quasi Geruest fuer einen Screen
        modifier = modifier,
        topBar = { TopAppBar(title = {Text("Alle Gruppen")}) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            //jede Spalte muss in ein item (bei einer LazyColum)
            //ein Zeilen: item vs. mehere Zeilen: items
            item(){
                Button(onClick = { /*weiter Leitung an GroupsFrom*/ },
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text("+ Gruppe")
                }
            }
            items (uiState.groups){ group ->
                //fuer jeden Namen in der names Liste wird ein Item mit einer Card erstellt
                Card(modifier = modifier.fillMaxWidth()) {
                    //durch die Card wird z.B. die Graue Box eingefuegt
                    Text(
                        text = "Gruppe ${group.name}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                        //der Modifier sorgt fur den Abstand (nach oben, unten, links und rechts) zwischen dem Text und grauen Kasten
                    )
                }
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
        GroupsScreen(testUiState)
    }
}

