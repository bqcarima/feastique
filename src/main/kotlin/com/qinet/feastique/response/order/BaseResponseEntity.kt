package com.qinet.feastique.response.order

import com.qinet.feastique.model.enums.OrderType
import java.util.UUID

sealed interface BaseResponseEntity {
    val id: UUID
    val quantity: Int
    val totalAmount: Long
    val orderType: String
}

