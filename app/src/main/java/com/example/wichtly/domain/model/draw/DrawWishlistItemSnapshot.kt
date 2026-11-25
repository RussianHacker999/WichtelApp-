package com.example.wichtly.domain.model.draw

import java.util.UUID

data class DrawWishlistItemSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sortOrder: Int,
)
