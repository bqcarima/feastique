package com.qinet.feastique.response.order

import java.util.UUID

data class FoodOrderCustomerResponse(
    val id: UUID,
    val username: String,
    val firstName: String,
    val lastName: String,
)

