package com.qinet.feastique.response.food

import java.util.*

data class FoodSizeResponse(
    val id: UUID,
    val size: String,
    val name: String?,
    val priceIncrease: Long
)

