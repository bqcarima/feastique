package com.qinet.feastique.response.food

import com.qinet.feastique.model.enums.Size

data class FoodSizeResponse(
    val id: Long,
    val size: Size,
    val priceIncrease: Long
)

