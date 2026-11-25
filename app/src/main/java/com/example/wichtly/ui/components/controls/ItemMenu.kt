package com.example.wichtly.ui.components.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.theme.WichtlyTheme

/** Ein Eintrag eines [ItemMenu]. Destruktive Einträge (Löschen) erscheinen in Rot. */
data class MenuAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val destructive: Boolean = false,
)

/**
 * Der ⋮-Button mit seinem Aufklappmenü. Aktionen auf Kartenebene (Bearbeiten, Löschen, Öffnen) liegen hier,
 * damit Karten keine Reihen kleiner Icons zeigen.
 */
@Composable
fun ItemMenu(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.common_more_actions),
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = contentDescription)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            actions.forEach { action ->
                val color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                DropdownMenuItem(
                    text = { Text(text = action.label, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(imageVector = action.icon, contentDescription = null) },
                    onClick = {
                        expanded = false
                        action.onClick()
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = color,
                        leadingIconColor = if (action.destructive) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF15392E)
@Composable
private fun ItemMenuPreview() {
    WichtlyTheme {
        ItemMenu(
            actions = listOf(
                MenuAction("Wunsch bearbeiten", Icons.Filled.Edit, {}),
                MenuAction("Wunsch löschen", Icons.Filled.Delete, {}, destructive = true),
            )
        )
    }
}
