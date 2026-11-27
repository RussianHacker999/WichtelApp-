package com.example.wichtly.ui.components.display

import java.math.BigDecimal
import java.math.RoundingMode
import com.example.wichtly.domain.validation.DrawDayInputFormatter
import java.time.LocalDate

/*
 * Formatting of stored values for display in the form (edit mode).
 *
 * The opposite direction (converting text input into cents or a LocalDate) is part of input
 * validation and therefore lives in the domain (domain/validation/GiftGroupInputParsing.kt).
 */

/**
 * Formats a value in cents back into a euro string for edit mode.
 *
 * Uses a comma as the decimal separator, because that is the standard in German.
 *
 * @param cents the amount in cents (e.g. 2550L).
 * @return the formatted euro string (e.g. "25,50").
 */
fun formatCentsForInput(cents: Long): String {
    val euros = BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.UNNECESSARY)
    return euros.toString().replace('.', ',')
}

/**
 * Formats a LocalDate back into the input format of the draw day
 * ([com.example.wichtly.domain.validation.DRAW_DAY_INPUT_PATTERN]), so that
 * [parseDate][com.example.wichtly.domain.validation.parseDate] can read it back unchanged.
 *
 * @return the string, e.g. "24.12.2026".
 */
fun formatDateForInput(date: LocalDate): String = date.format(DrawDayInputFormatter)
