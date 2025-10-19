package com.qinet.feastique.response.food

import java.util.UUID

data class FoodOrderTypeResponse(
    val id: UUID,
    val foodId: UUID,
    val orderType: String
)
