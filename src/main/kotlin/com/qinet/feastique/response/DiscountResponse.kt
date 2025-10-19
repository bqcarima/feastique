package com.qinet.feastique.response

import java.util.Date
import java.util.UUID

data class DiscountResponse(
    val id: UUID,
    val discountName: String,
    val percentage: Int,
    val startDate: Date,
    val endDate: Date
)
