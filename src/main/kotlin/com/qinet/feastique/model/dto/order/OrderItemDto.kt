package com.qinet.feastique.model.dto.order

import java.util.*

data class OrderItemDto(
    var id: UUID? = null,
    var foodId: UUID?,
    var foodQuantity: Int? = 1,
    var vendorId: UUID?, // only applicable to an order
    var complementId: UUID?,
    var addOnIds: List<UUID>?,
    var beverageIds: Map<UUID, Int>?,
    var foodSizeId: UUID?,
    var customerAddressId: UUID?, // only applicable to an order
    var orderType: String?,
)

