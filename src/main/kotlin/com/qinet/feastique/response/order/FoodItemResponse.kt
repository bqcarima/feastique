package com.qinet.feastique.response.order

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.food.FoodMinimalResponse
import com.qinet.feastique.response.food.FoodSizeResponse
import java.util.*

data class FoodItemResponse(
    override val id: UUID,
    override val food: FoodMinimalResponse,
    override val complement: ComplementResponse,
    val addOns: List<AddOnResponse>,
    override val size: FoodSizeResponse,
    override val quantity: Int,
    val discounts: List<DiscountResponse>,
    override val totalAmount: Long,
    override val orderType: String,
) : FoodResponseEntity

