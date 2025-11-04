package com.qinet.feastique.response.order

import java.util.*

sealed interface BaseResponseEntity {
    val id: UUID
    val quantity: Int
    val totalAmount: Long
    val orderType: String
}

