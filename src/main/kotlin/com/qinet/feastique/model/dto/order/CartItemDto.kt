package com.qinet.feastique.model.dto.order

import java.util.UUID
data class CartItemDto(
    val ids: List<UUID>,
    val deliveryAddress: UUID?
)

