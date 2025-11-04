package com.qinet.feastique.response.order

import com.qinet.feastique.response.BeverageResponse
import java.util.*

data class BeverageItemResponse(
    override val id: UUID,
    override val beverage: BeverageResponse,
    val unitPrice: Long,
    override val quantity: Int,
    override val orderType: String,
    override val totalAmount: Long
) : BeverageResponseEntity

