package com.qinet.feastique.response.order

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.food.FoodMinimalResponse
import com.qinet.feastique.response.food.FoodSizeResponse
import java.util.UUID

data class FoodOrderItemResponse(
    val id: UUID,
    val food: FoodMinimalResponse,
    val complement: ComplementResponse,
    val addOn: List<AddOnResponse>,
    val size: FoodSizeResponse,
    val discounts: List<DiscountResponse>,
    val totalAmount: Long,
)

