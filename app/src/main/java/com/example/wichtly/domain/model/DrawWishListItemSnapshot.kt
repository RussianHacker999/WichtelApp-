package com.example.wichtly.domain.model

import java.util.UUID

data class DrawWishListItemSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sortOrder: Int,
)
