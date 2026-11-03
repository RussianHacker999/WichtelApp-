package com.example.wichtly.domain.model

import java.time.LocalDate

data class GiftGroup(
    val id: String,
    val name: String,
    val budgetCents: Long,
    val drawDay: LocalDate,
    val drawType: DrawType = DrawType.NORMAL,
    val photoPath: String? = null
)
