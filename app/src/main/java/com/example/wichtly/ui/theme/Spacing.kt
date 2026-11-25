package com.example.wichtly.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Einzige Quelle für Abstände in Screens und Bausteinen; damit richtet sich jeder Screen gleich aus.
 *
 * Grundskala: xxs 2 · xs 4 · tight 6 · s 8 · inline 10 · m 12 · relaxed 14 · l 16 · xl 24 · xxl 32 dp.
 * Die Zwischenstufen (tight, inline, relaxed) sind benannt nach ihrem Einsatz im Design.
 * Rollen-Tokens (screen, section, cardGap, cardPadding) beschreiben wiederkehrende Layout-Stellen.
 * Ausnahme: formgebundene Maße eines einzelnen Bausteins (Geschenkanhänger, Startbild) stehen als
 * benannte Konstanten in dessen Datei. Größen (Avatar, Icon, Button) sind keine Abstände.
 */
object Spacing {
    /** Zwischen gestapelten Textzeilen (Titel und Untertitel). */
    val xxs = 2.dp
    val xs = 4.dp

    /** Eng: Label über Feld, gestapelte Kopfzeilen, Abstand zwischen Chips. */
    val tight = 6.dp
    val s = 8.dp

    /** Icon neben Text in Zeilen, Inhalte innerhalb einer Karte. */
    val inline = 10.dp
    val m = 12.dp

    /** Innenabstand von Pillen und Auswahlkarten, Abstand Kopf zu Untertitel. */
    val relaxed = 14.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Seitlicher Rand (links/rechts) der Listen-Screens. */
    val screen = 16.dp

    /** Abstand zwischen zwei Abschnitten eines Screens. */
    val section = 24.dp

    /** Abstand zwischen zwei Karten in einer Liste. */
    val cardGap = 12.dp

    /** Innenabstand einer Karte. */
    val cardPadding = 16.dp
}
