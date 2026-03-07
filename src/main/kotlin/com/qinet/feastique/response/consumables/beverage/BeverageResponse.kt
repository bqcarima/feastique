package com.qinet.feastique.response.consumables.beverage

import java.time.LocalTime
import java.util.UUID

data class BeverageResponse(
    val id: UUID,
    val beverageName: String,
    val alcoholic: Boolean,
    val percentage: Int,
    val beverageGroup: String,
    val deliverable: Boolean,
    val availability: String,
    val readyAsFrom: LocalTime?,
    val orderTypes: Set<String>,
    val beverageFlavours: Set<BeverageFlavourResponse>,
    val preparationTime: Int,
    val deliveryFee: Long,
)

data class BeverageFlavourResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val availability: String,
    val flavourSizes: Set<BeverageFlavourSizeResponse>
)

data class BeverageFlavourSizeResponse(
    val id: UUID,
    val size: String,
    val sizeName: String,
    val price: Long,
    val availability: String
)

