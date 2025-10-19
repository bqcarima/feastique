package com.qinet.feastique.response.food

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse
import java.time.LocalTime
import java.util.UUID

data class FoodResponse(
    val id: UUID,
    val foodNumber: String,
    val foodName: String,
    val vendorId: UUID,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val images: List<FoodImageResponse>,
    val size: List<FoodSizeResponse>,
    val basePrice: Long,
    val complements: List<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val preparationTime: Int,
    val orderType: List<FoodOrderTypeResponse>,
    val availability: List<FoodAvailabilityResponse>,
    val deliveryTime: LocalTime?,
    val deliveryFee: Long?,
    val discount: List<FoodDiscountResponse>
)

