package com.example.wichtly.domain.model

import java.time.LocalDate

data class Participant(
    val id: String,
    val groupId: String,
    val name: String,
    val photoPath: String? = null
)
