package com.example.wichtly.domain.model.group

import java.time.LocalDate
import java.util.UUID

/**
 * A gift group for one Secret Santa event.
 *
 * The participants are not part of this class; each participant references its group via `groupId`.
 *
 * @property budgetCents budget per present in cents, so amounts stay exact.
 * @property drawDay day of the exchange; draws are allowed up to and including this day.
 * @property photoPath path relative to the app's files directory; null if the group has no photo.
 */
data class GiftGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType = DrawType.NORMAL,
    val photoPath: String? = null,
)
