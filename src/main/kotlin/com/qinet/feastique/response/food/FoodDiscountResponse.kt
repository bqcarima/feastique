package com.qinet.feastique.response.food

import java.util.Date
import java.util.UUID

data class FoodDiscountResponse(
    val id: UUID,
    val discountName: String,
    val percentage: Int,
    val startDate: Date,
    val endDate: Date,
)
