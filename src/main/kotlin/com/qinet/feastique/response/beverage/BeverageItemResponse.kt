package com.qinet.feastique.response.beverage

import java.util.UUID

data class BeverageItemResponse(
    val id: UUID,
    val beverageName: String,
    val quantity: Int,
    val totalAmount: Long
)
