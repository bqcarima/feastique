package com.qinet.feastique.model.dto.order

import com.qinet.feastique.model.enums.OrderStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.*

data class ItemDto (
    val quickDelivery: Boolean,
    var customerAddressId: UUID?, // only applicable to an order
    var beverageItemDto: BeverageItemDto?,
    var dessertItemDto: DessertItemDto?,
    var foodItemDto: FoodItemDto?,
    var handheldItemDto: HandheldItemDto?,
    var orderType: String?
)
data class BeverageItemDto (

    var id: UUID? = null,

    @field:NotNull(message = "Beverage Id cannot be empty.")
    var beverageId: UUID,

    @field:NotNull(message = "Beverage flavour Id cannot be empty.")
    var beverageFlavourId: UUID,

    @field:NotNull(message = "Beverage flavour size Id cannot be empty.")
    var beverageFlavourSizeId: UUID,

    val quantity: Int? = 1,
)

data class CartItemDto(
    val ids: List<UUID>,
    val deliveryAddress: UUID?,
    val quickDelivery: Boolean
)

data class ChangeQuantityDto(
    @field:Min(value = 0, message = "Value cannot be less that 0.")
    var quantity: Int? = 1
)

data class DessertItemDto(
    var id: UUID? = null,

    @field:NotNull(message = "Dessert Id cannot be empty.")
    var dessertId: UUID,

    @field:NotNull(message = "Dessert flavour Id cannot be empty.")
    var dessertFlavourId: UUID,

    @field:NotNull(message = "Dessert flavour size Id cannot be empty.")
    var dessertFlavourSizeId: UUID,

    val quantity: Int? = 1,
)

data class FoodItemDto(
    var id: UUID? = null,

    @field:NotNull(message = "Food Id cannot be empty.")
    var foodId: UUID,

    var foodQuantity: Int? = 1,

    @field:NotNull(message = "Complement Id cannot be empty.")
    var complementId: UUID?,

    var addOnIds: Set<UUID>?,

    @field:NotNull(message = "Food size Id cannot be empty.")
    var foodSizeId: UUID?,
)

data class HandheldItemDto(
    var id: UUID? = null,

    @field:NotNull(message = "Handheld Id cannot be empty.")
    var handheldId: UUID,

    @field:NotNull(message = "Handheld size Id cannot be empty.")
    var handheldSizeId: UUID?,

    var quantity: Int? = 1,
)

data class OrderUpdateDto(
    val orderStatus: OrderStatus
)

