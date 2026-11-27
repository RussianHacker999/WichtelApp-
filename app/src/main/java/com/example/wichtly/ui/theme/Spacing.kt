package com.example.wichtly.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Single source of spacing in screens and building blocks, so every screen is aligned the same way.
 *
 * Base scale: xxs 2 · xs 4 · tight 6 · s 8 · inline 10 · m 12 · relaxed 14 · l 16 · xl 24 · xxl 32 dp.
 * The intermediate steps (tight, inline, relaxed) are named after their use in the design.
 * Role tokens (screen, cardGap, cardPadding) describe recurring layout positions.
 * Exception: fixed dimensions of a single building block (gift tag, start image) are named
 * constants in its own file. Sizes (avatar, icon, button) are not spacing.
 */
object Spacing {
    /** Between stacked text lines (title and subtitle). */
    val xxs = 2.dp
    val xs = 4.dp

    /** Tight: label above a field, stacked header lines, gap between chips. */
    val tight = 6.dp
    val s = 8.dp

    /** Icon next to text in rows, content inside a card. */
    val inline = 10.dp
    val m = 12.dp

    /** Padding of pills and selection cards, gap between header and subtitle. */
    val relaxed = 14.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Side margin (left/right) of the list screens. */
    val screen = 16.dp

    /** Gap between two cards in a list. */
    val cardGap = 12.dp

    /** Padding inside a card. */
    val cardPadding = 16.dp
}
