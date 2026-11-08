package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wichtly.domain.model.DrawType
import java.time.LocalDate

@Entity(tableName = "gift_groups")
data class GiftGroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType = DrawType.NORMAL,
    val photoPath: String? = null
)
