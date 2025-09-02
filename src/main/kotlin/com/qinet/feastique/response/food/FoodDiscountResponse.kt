package com.qinet.feastique.response.food

import java.util.Date

data class FoodDiscountResponse(
    val id: Long,
    val discountName: String,
    val percentage: Int,
    val startDate: Date,
    val endDate: Date,
)
