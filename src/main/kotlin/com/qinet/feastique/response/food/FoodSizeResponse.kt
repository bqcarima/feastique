package com.qinet.feastique.response.food

import com.qinet.feastique.model.enums.Size
import java.util.UUID

data class FoodSizeResponse(
    val id: UUID,
    val size: Size,
    val priceIncrease: Long
)

