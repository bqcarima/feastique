package com.qinet.feastique.response.food

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.ComplementResponse

data class FoodResponse(
    val id: Long,
    val foodName: String,
    val vendorId: Long,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val basePrice: Long,
    val images: List<FoodImageResponse>,
    val size: List<FoodSizeResponse>,
    val complements: List<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val orderType: List<FoodOrderTypeResponse>,
    val availability: List<FoodAvailabilityResponse>,
    val discount: List<FoodDiscountResponse>
)

