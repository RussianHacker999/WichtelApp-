package com.example.wichtly.domain.validation

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/*
 * Converts the text input for budget and date into internal data types.
 *
 * Lives in the domain so that every check of a group happens in exactly one place:
 * SaveGiftGroupUseCase takes the raw texts, converts them here and reports format errors and
 * business errors together in one result, so the form shows all errors on the first save.
 * The opposite direction (display in the form) stays in the UI (GroupFormInput.kt).
 */

/**
 * Parses a text input for a budget in euros into a value in cents.
 *
 * Uses [BigDecimal] instead of Double, because floating point numbers cannot represent amounts
 * such as 0.10 exactly (0.1 + 0.2 != 0.3); BigDecimal calculates exactly in decimal.
 *
 * @param input the user input, e.g. "25", "25,5", "25.50", "0,99".
 * @return the value in cents, or null if the input is empty, not numeric, negative or has more
 *         than two decimal places (e.g. "1,234"). A correctly written amount that is too large for
 *         Long yields [Long.MAX_VALUE], so the domain reports it as "too high" instead of a format error.
 */
fun parseBudgetToCents(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    // German keyboards produce "25,50"; BigDecimal expects a dot
    val normalized = trimmed.replace(',', '.')

    // Only non-negative integers or decimals with at most two decimal places; rules out
    // negative numbers, several decimal separators and more than two decimal places.
    if (!normalized.matches(Regex("""^\d+(\.\d{1,2})?$"""))) {
        return null
    }

    return try {
        BigDecimal(normalized)
            .multiply(BigDecimal(100))
            .longValueExact()
    } catch (_: ArithmeticException) {
        Long.MAX_VALUE
    }
}

/**
 * Input format of the draw day, e.g. "24.12.2026". The only definition of this pattern:
 * [parseDate] reads with it, the UI (formatDateForInput in edit mode) writes with it.
 */
const val DRAW_DAY_INPUT_PATTERN = "dd.MM.uuuu"

/** Formatter for [DRAW_DAY_INPUT_PATTERN]; STRICT, see [parseDate]. */
val DrawDayInputFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern(DRAW_DAY_INPUT_PATTERN)
    .withResolverStyle(ResolverStyle.STRICT)

/**
 * Parses a date input in the format [DRAW_DAY_INPUT_PATTERN] into a [LocalDate].
 *
 * Uses ResolverStyle.STRICT, because the default parsing silently corrects invalid dates
 * (e.g. 31.02.2026 becomes 03.03.2026); STRICT rejects invalid days instead. STRICT requires
 * the year pattern "uuuu" instead of "yyyy".
 *
 * @param input the date input, e.g. "24.12.2026".
 * @return the parsed date, or null if the format or the date is invalid.
 */
fun parseDate(input: String): LocalDate? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    return try {
        LocalDate.parse(trimmed, DrawDayInputFormatter)
    } catch (e: DateTimeParseException) {
        null
    }
}
