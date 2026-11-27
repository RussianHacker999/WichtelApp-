package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wichtly.domain.model.group.DrawType
import java.time.LocalDate

/**
 * Room table `gift_groups`: the top-level table; all other tables depend on it.
 *
 * @property budgetCents budget in cents, so amounts are stored exactly.
 * @property photoPath path relative to the app's files directory; null if the group has no photo.
 */
@Entity(tableName = "gift_groups")
data class GiftGroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType,
    val photoPath: String?
)
