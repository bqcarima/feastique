package com.qinet.feastique.response.food

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse
import java.time.LocalTime

data class FoodResponse(
    val id: Long,
    val foodName: String,
    val vendorId: Long,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val images: List<FoodImageResponse>,
    val size: List<FoodSizeResponse>,
    val basePrice: Long,
    val complements: List<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val preparationTime: Long,
    val orderType: List<FoodOrderTypeResponse>,
    val availability: List<FoodAvailabilityResponse>,
    val deliveryTime: LocalTime?,
    val deliveryFee: Long?,
    val discount: List<FoodDiscountResponse>
)

