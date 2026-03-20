package com.qinet.feastique.response.consumables.food

import java.util.Date
import java.util.UUID

data class FoodDiscountResponse(
    val id: UUID,
    val discountName: String,
    val percentage: Int,
    val startDate: Date,
    val endDate: Date,
)

data class FoodSizeResponse(
    val id: UUID,
    val size: String,
    val name: String?,
    val priceIncrease: Long,
    val availability: String
)

