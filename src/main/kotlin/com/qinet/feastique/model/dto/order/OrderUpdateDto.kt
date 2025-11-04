package com.qinet.feastique.model.dto.order

import com.qinet.feastique.model.enums.OrderStatus

data class OrderUpdateDto(
    val orderStatus: OrderStatus
)

