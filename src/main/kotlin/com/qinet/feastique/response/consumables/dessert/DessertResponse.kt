package com.qinet.feastique.response.consumables.dessert

import com.qinet.feastique.response.discount.DiscountResponse
import com.qinet.feastique.response.image.ImageResponse
import java.time.LocalTime
import java.util.*

data class DessertResponse(
    val id: UUID,
    val dessertName: String,
    val description: String?,
    val dessertType: String,
    val availability: String,
    val deliverable: Boolean,
    val deliveryFee: Long,
    val dessertFlavours: List<DessertFlavourResponse>,
    val dessertImages: List<ImageResponse>,
    val preparationTime: Int,
    val readyAsFrom: LocalTime?,
    val orderTypes: List<String>,
    val availableDays: List<String>,
    val discounts: Set<DiscountResponse>
)

data class DessertFlavourResponse(
    val id: UUID,
    val flavourName: String,
    val description: String?,
    val availability: String,
    val flavourSizes: List<DessertFlavourSizeResponse>
)

data class DessertFlavourSizeResponse(
    val id: UUID,
    val size: String,
    val sizeName: String?,
    val price: Long,
    val availability: String,
)

data class DessertOrderResponse(
    val dessert: String,
    val dessertFlavour: String,
    val dessertFlavourSize: String
)

