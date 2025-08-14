package com.qinet.feastique.response

import java.util.Date

data class DiscountResponse(
    val id: Long,
    val discountName: String,
    val percentage: Int,
    val startDate: Date,
    val endDate: Date
)
