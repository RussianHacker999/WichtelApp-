package com.example.wichtly.ui.components.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/** Kleine Beschriftung über einem Formularfeld. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground, modifier = modifier)
}

/**
 * Kürzt [text] auf höchstens [maxLength] Zeichen, ohne ein Emoji (Surrogatpaar) zu zerschneiden.
 * Genutzt von [WichtlyTextField], damit Eingabe und Einfügen die Domain-Grenze nicht überschreiten.
 */
internal fun limitToLength(text: String, maxLength: Int): String {
    if (text.length <= maxLength) return text
    val cut = if (maxLength > 0 && Character.isHighSurrogate(text[maxLength - 1])) maxLength - 1 else maxLength
    return text.substring(0, cut)
}

/**
 * Textfeld mit Beschriftung darüber. Ein Fehlertext ersetzt den Hilfetext; mit [maxLength] erscheint
 * zusätzlich ein Zeichenzähler.
 */
@Composable
fun WichtlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    maxLength: Int? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
        FieldLabel(text = label)
        val helper = errorText ?: supportingText
        OutlinedTextField(
            value = value,
            // Mit maxLength lässt das Feld keine längere Eingabe zu (auch nicht per Einfügen);
            // die verbindliche Prüfung bleibt trotzdem in der Domain.
            onValueChange = { input -> onValueChange(if (maxLength != null) limitToLength(input, maxLength) else input) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            placeholder = placeholder?.let { { Text(text = it) } },
            trailingIcon = trailingIcon,
            supportingText = if (helper == null && maxLength == null) null else {
                {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        Text(text = helper.orEmpty(), modifier = Modifier.weight(1f))
                        if (maxLength != null) {
                            // Zeichenzähler, z. B. "37/50"
                            Text(text = "${value.length}/$maxLength")
                        }
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

/** Auswahlkarte über die volle Breite mit Radiobutton (z. B. Wichtelart). */
@Composable
fun ChoiceCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
                shape,
            )
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Spacing.m, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Hinweisleiste mit Icon, Text und optionalem Aktions-Link (z. B. Kamerazugriff abgelehnt).
 */
@Composable
fun HintBar(
    text: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.PhotoCamera,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = Spacing.relaxed, end = Spacing.s, top = Spacing.m, bottom = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.inline),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            if (actionLabel != null && onAction != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(role = Role.Button, onClick = onAction)
                        .padding(vertical = Spacing.s),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun FormFieldsPreview() {
    WichtlyTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WichtlyTextField(label = "Gruppenname *", value = "Familie Wichteln", onValueChange = {})
            WichtlyTextField(label = "Geschenkbudget (€) *", value = "25,5x", onValueChange = {}, errorText = "Ungültiges Budget.")
            ChoiceCard(title = "Klassisch", description = "Jeder schenkt einer Person.", selected = true, onClick = {})
            ChoiceCard(title = "Schrottwichteln", description = "Kurioses statt Nützliches.", selected = false, onClick = {})
            HintBar(text = "Kamerazugriff wurde abgelehnt. Du kannst ohne Foto fortfahren.", actionLabel = "Einstellungen öffnen", onAction = {})
        }
    }
}
