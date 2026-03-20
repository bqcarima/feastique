package com.qinet.feastique.response.consumables.handheld

import com.qinet.feastique.response.consumables.BaseEntityResponse
import com.qinet.feastique.response.discount.DiscountResponse
import com.qinet.feastique.response.image.ImageResponse
import java.time.LocalTime
import java.util.*

data class HandheldResponse(
    override val id: UUID,
    val handheldNumber: String,
    val name: String,
    val vendorId: UUID,
    val vendorName: String,
    val description: String?,
    val images: List<ImageResponse>,
    override val likeCount: Long,
    override val likedByCurrentUser: Boolean,
    override val bookmarkCount: Long,
    override val bookmarkedByCurrentUser: Boolean,
    val sizes: List<HandheldSizeResponse>,
    val fillings: List<FillingResponse>,
    val availability: String,
    val preparationTime: Int,
    val readyAsFrom: LocalTime?,
    val orderTypes: Set<String>,
    val handheldType: String?,
    val availableDays: Set<String>,
    val deliverable: Boolean,
    val deliveryFee: Long,
    val discounts: Set<DiscountResponse>

) : BaseEntityResponse

data class HandheldMinimalResponse(
    val id: UUID,
    val handheldNumber: String,
    val handheldName: String,
    val description: String?,
    val likeCount: Long,
    val likedByCurrentUser: Boolean,
)

data class FillingResponse(
    val id: UUID,
    val fillingName: String,
    val description: String?,
)

data class FillingOrderResponse(
    val name: String
)

data class HandheldSizeResponse(
    val id: UUID,
    val numberOfFillings: Long,
    val size: String,
    val sizeName: String?,
    val price: Long,
    val availability: String?
)

data class HandheldOrderResponse(
    val handheld: String,
    val fillings: Set<FillingOrderResponse>,
    val handheldSize: String
)

