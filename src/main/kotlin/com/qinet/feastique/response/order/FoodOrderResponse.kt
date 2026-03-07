package com.qinet.feastique.response.order

import java.util.UUID


data class FoodOrderCustomerResponse(
    val id: UUID,
    val username: String,
    val firstName: String,
    val lastName: String,
)

data class FoodOrderVendorResponse(
    val id: UUID,
    val username: String,
    val chefName: String,
    val restaurantName: String,
)