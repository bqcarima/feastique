package com.qinet.feastique.response.consumables.beverage

import com.qinet.feastique.response.consumables.BaseEntityResponse
import java.time.LocalTime
import java.util.UUID

data class BeverageResponse(
    override val id: UUID,
    val name: String,
    val alcoholic: Boolean,
    val percentage: Int,
    val beverageGroup: String,
    override val likeCount: Long,
    override val likedByCurrentUser: Boolean,
    override val bookmarkCount: Long,
    override val bookmarkedByCurrentUser: Boolean,
    val deliverable: Boolean,
    val availability: String,
    val readyAsFrom: LocalTime?,
    val preparationTime: Int,
    val deliveryFee: Long,
    val beverageFlavours: Set<BeverageFlavourResponse>,
    val availableDays: Set<String>,
    val orderTypes: Set<String>,

    ) : BaseEntityResponse

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

data class BeverageOrderResponse(
    val beverage: String,
    val beverageFlavour: String,
    val beverageFlavourSize: String
)

