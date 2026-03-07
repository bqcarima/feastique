package com.qinet.feastique.response.consumables.food

import com.qinet.feastique.response.ImageResponse
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
    val id: UUID,
    val foodNumber: String,
    val foodName: String,
    val vendorId: UUID,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val images: List<ImageResponse>,
    val availability: String,
    val size: List<FoodSizeResponse>,
    val basePrice: Long,
    val complements: List<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val preparationTime: Int,
    val readyAsFrom: LocalTime?,
    val orderType: List<String>,
    val availableDays: List<String>,
    val deliverable : Boolean,
    val dailyDeliveryQuantity: Int? = null,
    val deliveryTime: LocalTime?,
    val deliveryFee: Long?,
    val discount: List<FoodDiscountResponse>
)

data class FoodMinimalResponse(
    val id: UUID,
    val foodName: String,
    val mainCourse: String,
    val basePrice: Long
)

