package com.example.wichtly.domain.model.participant

import java.util.UUID

data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val name: String,
    val photoPath: String? = null
)
