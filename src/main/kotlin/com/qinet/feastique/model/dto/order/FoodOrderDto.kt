package com.qinet.feastique.model.dto.order

import com.qinet.feastique.model.enums.OrderType
import jakarta.validation.constraints.NotNull

data class FoodOrderDto(
    var id: Long? = null,

    @field:NotNull(message = "food Id cannot be empty.")
    var foodId: Long?,

    @field:NotNull(message = "Vendor Id cannot be empty.")
    var vendorId: Long?,

    @field:NotNull(message = "Complement Id cannot be empty.")
    var complementId: Long?,

    var addOnIds: List<Long>?,
    var beverageIds: List<Long>?,

    @field:NotNull(message = "Food size Id cannot be empty.")
    var foodSizeId: Long?,
    var customerAddressId: Long?,

    @field:NotNull(message = "Order type cannot be empty.")
    var orderType: OrderType?,
)