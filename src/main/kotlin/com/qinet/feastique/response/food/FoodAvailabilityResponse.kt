package com.qinet.feastique.response.food

import java.util.UUID

data class FoodAvailabilityResponse(
    val id: UUID,
    val availability: String,
)
