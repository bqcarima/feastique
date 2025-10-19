package com.qinet.feastique.response.food

import java.util.UUID

data class FoodImageResponse(
    val id: UUID,
    val imageUrl: String,
    val foodId: UUID,
)

