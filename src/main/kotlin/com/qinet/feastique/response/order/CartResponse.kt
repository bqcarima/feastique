package com.qinet.feastique.response.order

import java.util.*

data class CartResponse(
    val id: UUID,
    val items: List<BaseResponseEntity>,
    val total: Long
)

