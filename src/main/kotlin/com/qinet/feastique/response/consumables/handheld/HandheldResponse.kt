package com.qinet.feastique.response.consumables.handheld

import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.ImageResponse
import java.time.LocalTime
import java.util.UUID

data class HandheldResponse(
    val id: UUID,
    val handheldNumber: String,
    val handheldName: String,
    val vendorId: UUID,
    val vendorName: String,
    val description: String?,
    val images: List<ImageResponse>,
    val sizes: List<HandheldSizeResponse>,
    val fillings: List<FillingResponse>,
    val availability: String,
    val preparationTime: Int,
    val readyAsFrom: LocalTime?,
    val orderType: List<String>,
    val handheldType: String?,
    val availableDays: List<String>,
    val deliverable: Boolean,
    val deliveryFee: Long,
    val discounts: Set<DiscountResponse>
)

data class HandheldMinimalResponse(
    val id: UUID,
    val handheldNumber: String,
    val handheldName: String,
    val description: String?
)

data class FillingResponse(
    val id: UUID,
    val fillingName: String,
    val description: String?,
)

data class HandheldSizeResponse(
    val id: UUID,
    val numberOfFillings: Long,
    val size: String,
    val sizeName: String?,
    val price: Long,
    val availability: String?
)