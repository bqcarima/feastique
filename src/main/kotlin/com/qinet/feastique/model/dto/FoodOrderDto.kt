package com.qinet.feastique.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class FoodOrderDto(
    var id: Long? = null,

    @field:NotNull(message = "Vendor Id cannot be empty.")
    var vendorId: Long?,

    @field:NotBlank(message = "Food Id cannot be empty.")
    var foodId: Long?,

    @field:NotBlank(message = "Main course cannot be empty.")
    var foodName: String?,

    @field:NotBlank(message = "Main course cannot be empty.")
    var mainCourse: String?,

    @field:NotBlank(message = "Complement Id cannot be empty.")
    var complementId: Long?,

    var addOnIds: List<Long>,
    var beverageIds: List<Long>,
    var foodSizeId: Long?,
    var customerAddressId: Long?,

    @field:NotNull(message = "Delivery time cannot be empty.")
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var deliveryTime: LocalTime?,

    var deliveryFee: Long?,

    @field:NotNull(message = "Total cannot be null.")
    @field:Min(value = 1, message = "Total amount cannot be 0.")
    var totalAmount: Long?,

    @field:NotNull(message = "Order type cannot be empty.")
    var orderType: OrderType?,

    @field:NotNull(message = "Total cannot be empty.")
    var orderStatus: OrderStatus?,

    var customerDeletedStatus: Boolean?,
    var vendorDeletedStatus: Boolean?
)

