package com.example.wichtly.domain.model.draw

import java.util.UUID

data class DrawAssignment(
    val id: String = UUID.randomUUID().toString(),
    val giverName: String,
    val giverId: String,
    val receiverName: String,
    val receiverId: String,
    val receiverWishlistItems: List<DrawWishlistItemSnapshot>,
)
