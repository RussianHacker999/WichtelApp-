package com.example.wichtly.domain.model.group

/**
 * Type of a draw.
 *
 * Display texts do not belong in the domain layer; they live in `strings.xml` and are mapped in the
 * UI via [com.example.wichtly.ui.components.display.labelRes].
 *
 * Room stores the constant name as text (Converters.fromDrawType). Do not rename or delete constants,
 * otherwise groups and draws saved earlier can no longer be read. Adding new constants is fine.
 */
enum class DrawType {
    NORMAL,
    JUNK,
}
