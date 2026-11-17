package com.example.wichtly.ui.draws

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Anzeige der gezogenen Zuordnung (Schenker -> Beschenkter)
 * inklusive der Wünsche des Beschenkten.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) nimmt die Komponente die fertigen Zuordnungsdaten entgegen
 * und löst nach Bestätigung lediglich das Schließen über [onDismiss] aus.
 *
 * @param assignment Die anzuzeigende Ziehungs-Zuordnung mit Schenker, Beschenktem und Wunschliste.
 * @param onDismiss Callback beim Schließen des Ergebnis-Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun DrawResultDialog(
    assignment: TemporaryDrawAssignment,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${assignment.giverName} → ${assignment.receiverName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Du beschenkst ${assignment.receiverName}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Wunschliste von ${assignment.receiverName}:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (assignment.wishes.isEmpty()) {
                            Text(
                                text = "Zum Zeitpunkt der Auslosung waren keine Wünsche eingetragen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            assignment.wishes.forEach { wish ->
                                Text(
                                    text = "• $wish",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Schließen")
            }
        },
        modifier = modifier
    )
}

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

/**
 * Preview 1: Ergebnisdialog mit drei Wünschen.
 */
@Preview
@Composable
private fun DrawResultDialogWithWishesPreview() {
    val sampleAssignment = TemporaryDrawAssignment(
        giverId = "p1",
        giverName = "Diana",
        receiverId = "p2",
        receiverName = "Melissa",
        wishes = listOf(
            "Schmuck",
            "Fantasy Bücher",
            "bunte Textmarker"
        )
    )

    WichtlyTheme {
        DrawResultDialog(
            assignment = sampleAssignment,
            onDismiss = {}
        )
    }
}

/**
 * Preview 2: Ergebnisdialog ohne Wünsche (leere Wunschliste).
 */
@Preview
@Composable
private fun DrawResultDialogEmptyWishesPreview() {
    val sampleAssignment = TemporaryDrawAssignment(
        giverId = "p1",
        giverName = "Diana",
        receiverId = "p2",
        receiverName = "Robin",
        wishes = emptyList()
    )

    WichtlyTheme {
        DrawResultDialog(
            assignment = sampleAssignment,
            onDismiss = {}
        )
    }
}

/**
 * Preview 3: Ergebnisdialog mit einem sehr langen Wunschtext.
 */
@Preview
@Composable
private fun DrawResultDialogLongWishTextPreview() {
    val sampleAssignment = TemporaryDrawAssignment(
        giverId = "p1",
        giverName = "Diana",
        receiverId = "p2",
        receiverName = "Melissa",
        wishes = listOf(
            "Ein sehr ausführlicher und detaillierter Wunsch über mehrere Zeilen: Ein gebundenes Buch mit grünem Stoffeinband, mindestens 500 Seiten, aus dem Genre Epic Fantasy."
        )
    )

    WichtlyTheme {
        DrawResultDialog(
            assignment = sampleAssignment,
            onDismiss = {}
        )
    }
}
