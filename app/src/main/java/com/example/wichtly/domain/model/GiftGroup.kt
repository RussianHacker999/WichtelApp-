package com.example.wichtly.domain.model

import java.time.LocalDate
import java.util.UUID

data class GiftGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType = DrawType.NORMAL,
    val photoPath: String? = null
)
