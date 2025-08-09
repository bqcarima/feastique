package com.qinet.feastique.response

data class FoodResponse(
    val id: Long,
    val foodName: String,
    val vendorId: Long,
    val vendorName: String,
    val mainCourse: String,
    val description: String,
    val basePrice: String,
    val size: List<FoodSizeResponse>,
    val complements: List<ComplementResponse>,
    val addOn: List<AddOnResponse>,
    val orderType: List<String>,
    val image: List<String>
)

