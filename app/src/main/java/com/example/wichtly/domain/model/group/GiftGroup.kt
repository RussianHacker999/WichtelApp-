package com.example.wichtly.domain.model.group

import java.time.LocalDate
import java.util.UUID

/**
 * A group for a Secret Santa event.
 * Info:
 * The participants are not saved in this data class.
 * The participants save the groupId from their group.
 *
 * @param id  unique id from the GiftGroup object
 * @param name  the name of the group
 * @param budgetCents  the budget in cents for one present
 * @param drawDay  the day of the draw
 * @param drawType  the type of the draw (normal or junk)
 * @param photoPath  the path to the photo of the group (null is valid)
 */
data class GiftGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType = DrawType.NORMAL,
    val photoPath: String? = null
)
