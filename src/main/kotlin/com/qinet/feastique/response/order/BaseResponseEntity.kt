package com.qinet.feastique.response.order

import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.consumables.food.AddOnResponse
import com.qinet.feastique.response.consumables.food.ComplementResponse
import com.qinet.feastique.response.consumables.food.FoodMinimalResponse
import com.qinet.feastique.response.consumables.food.FoodSizeResponse
import java.util.*

sealed interface BaseResponseEntity {
    val id: UUID
    val quantity: Int
    val totalAmount: Long
    val orderType: String
}


sealed interface BeverageResponseEntity : BaseResponseEntity {
    val beverage: BeverageResponse
}

data class BeverageItemResponse(
    override val id: UUID,
    override val beverage: BeverageResponse,
    val unitPrice: Long,
    override val quantity: Int,
    override val orderType: String,
    override val totalAmount: Long
) : BeverageResponseEntity

sealed interface DessertResponseEntity : BaseResponseEntity {
    val dessert : DessertResponse
}

data class DessertItemResponse(
    override val id: UUID,
    override val dessert: DessertResponse,
    val unitPrice: Long,
    override val quantity: Int,
    override val totalAmount: Long,
    override val orderType: String
) : DessertResponseEntity


sealed interface FoodResponseEntity : BaseResponseEntity {
    val food: FoodMinimalResponse
    val complement: ComplementResponse
    val size: FoodSizeResponse
}

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


data class UnknownEntityResponse(
    // Generate a random UUID that is never saved to fulfil non-null constraint
    override val id: UUID = UuidCreator.getTimeOrdered(),
    override val quantity: Int,
    override val totalAmount: Long,
    override val orderType: String
) : BaseResponseEntity

