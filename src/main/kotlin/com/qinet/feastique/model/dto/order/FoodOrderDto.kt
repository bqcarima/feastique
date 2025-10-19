package com.qinet.feastique.model.dto.order

import com.qinet.feastique.model.enums.OrderType
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class FoodOrderDto(

    var id: UUID? = null,

    @field:NotNull(message = "food Id cannot be empty.")
    var foodId: UUID?,
    var foodQuantity: Int? = 1,

    @field:NotNull(message = "Vendor Id cannot be empty.")
    var vendorId: UUID?,

    @field:NotNull(message = "Complement Id cannot be empty.")
    var complementId: UUID?,

    var addOnIds: List<UUID>?,
    var beverageIds: Map<UUID, Int>?,

    @field:NotNull(message = "Food size Id cannot be empty.")
    var foodSizeId: UUID?,
    var customerAddressId: UUID?,

    @field:NotNull(message = "Order type cannot be empty.")
    var orderType: OrderType?,
)

