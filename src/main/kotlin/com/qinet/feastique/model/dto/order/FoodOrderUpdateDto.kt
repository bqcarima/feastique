package com.qinet.feastique.model.dto.order

import com.qinet.feastique.model.enums.OrderStatus

data class FoodOrderUpdateDto(
    val orderStatus: OrderStatus
)

