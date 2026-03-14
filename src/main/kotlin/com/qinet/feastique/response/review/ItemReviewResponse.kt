package com.qinet.feastique.response.review

import com.qinet.feastique.response.consumables.beverage.BeverageOrderResponse
import com.qinet.feastique.response.consumables.dessert.DessertOrderResponse
import com.qinet.feastique.response.consumables.food.FoodOrderResponse
import com.qinet.feastique.response.consumables.handheld.HandheldOrderResponse
import com.qinet.feastique.response.order.OrderReviewResponse
import java.util.*

sealed interface BaseReviewResponse {
    val id: UUID
    val order: OrderReviewResponse?
    val review: String?
    val rating: Float
}
data class BeverageOrderItemReviewResponse(
    override val id: UUID,
    override val order: OrderReviewResponse?,
    val beverageOrderItem: BeverageOrderResponse,
    override val review: String?,
    override val rating: Float,

) : BaseReviewResponse

data class DessertOrderItemReviewResponse(
    override val id: UUID,
    override val order: OrderReviewResponse?,
    val dessertOrderItem: DessertOrderResponse,
    override val review: String?,
    override val rating: Float

) : BaseReviewResponse

data class FoodOrderItemReviewResponse(
    override val id: UUID,
    override val order: OrderReviewResponse?,
    val food: FoodOrderResponse,
    override val review: String?,
    override val rating: Float

) : BaseReviewResponse

data class HandheldOrderItemReviewResponse(
    override val id: UUID,
    override val order: OrderReviewResponse?,
    val handheldOrderItem: HandheldOrderResponse,
    override val review: String?,
    override val rating: Float

) : BaseReviewResponse

data class VendorOrderReviewResponse(
    override val id: UUID,
    override val order: OrderReviewResponse?,
    override val review: String?,
    override val rating: Float

) : BaseReviewResponse