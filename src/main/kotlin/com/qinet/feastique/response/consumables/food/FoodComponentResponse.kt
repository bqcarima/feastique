package com.qinet.feastique.response.consumables.food

import com.qinet.feastique.response.consumables.BaseEntityResponse
import com.qinet.feastique.response.image.ImageResponse
import java.time.LocalTime
import java.util.*


data class AddOnResponse(
    val id: UUID,
    val addOnName: String,
    val price: Long,
    val availability: String
)

data class ComplementResponse(
    val id: UUID,
    val name: String,
    val price: Long,
    val availability: String
)

data class FoodResponse(
    override val id: UUID,
    val foodNumber: String,
    val name: String,
    val vendorId: UUID,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val images: Set<ImageResponse>,
    override val likeCount: Long,
    override val likedByCurrentUser: Boolean,
    override val bookmarkCount: Long,
    override val bookmarkedByCurrentUser: Boolean,
    val availability: String,
    val size: Set<FoodSizeResponse>,
    val basePrice: Long,
    val complements: Set<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val preparationTime: Int,
    val readyAsFrom: LocalTime?,
    val orderTypes: Set<String>,
    val availableDays: Set<String>,
    val deliverable : Boolean,
    val dailyDeliveryQuantity: Int? = null,
    val deliveryTime: LocalTime?,
    val deliveryFee: Long,
    val discount: Set<FoodDiscountResponse>

) : BaseEntityResponse

data class FoodMinimalResponse(
    val id: UUID,
    val foodName: String,
    val mainCourse: String,
    val basePrice: Long,
    val likeCount: Long,
    val likedByCurrentUser: Boolean,
)

data class FoodOrderResponse(
    val name: String,
    val complement: FoodOrderItemResponse,
    val addOns: Set<FoodOrderItemResponse>,
)
data class FoodOrderItemResponse(
    val name: String,
    val price: Long,
)

