package com.example.wichtly.domain.model.group

/**
 * Type of draw.
 * Display texts do not belong in the domain layer; they are located in `strings.xml` and
 * are mapped in the UI via `[com.example.wichtly.ui.components.display.labelRes]`.
 *
 * Attention: Room stores the constant name as text (Converters.fromDrawType).
 * Do not rename or delete constants
 *        -> It would make it impossible to read previously saved groups and draws.
 * Adding new constants is perfectly fine.
 */
enum class DrawType {
    NORMAL,
    JUNK,
}
