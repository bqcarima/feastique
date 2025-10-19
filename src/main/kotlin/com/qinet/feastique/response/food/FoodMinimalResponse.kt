package com.qinet.feastique.response.food

import java.util.UUID

data class FoodMinimalResponse(
    val id: UUID,
    val foodName: String,
    val mainCourse: String,
    val basePrice: Long
)
