package com.qinet.feastique.model.dto.consumables

import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.discount.DiscountDto
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalTime
import java.util.*

data class FoodDto(

    var id: UUID? = null,

    @field:NotBlank(message = "Food name cannot be empty.")
    var foodName: String?,

    @field:NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String?,

    @field:NotBlank(message = "Description cannot be null.")
    @field:NotEmpty(message = "Description cannot be empty.")
    var description: String?,

    @field:NotNull(message = "Price cannot be empty.")
    @field:Min(value = 1, message = "Base price cannot be 0.")
    var basePrice: Long?,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String,

    @field:NotNull(message = "Deliverability must be specified.")
    var deliverable: Boolean?,

    var readyAsFrom: LocalTime?,

    var dailyDeliveryQuantity: Int?,

    @field:NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int?,

    var quickDelivery: Boolean?,
    var deliveryTime: LocalTime?,
    var deliveryFee: Long?,
    var foodImages: Set<@Valid ImageDto>,
    var foodSizes: Set<@Valid FoodSizeDto>,
    var complements: Set<@Valid ComplementDto>,
    var addOns: Set<@Valid AddOnDto>?,
    var orderTypes: Set<String>,
    var availableDays: Set<String>,
    var discounts: Set<@Valid DiscountDto>?,
)

data class FoodSizeDto(
    var id: UUID?,
    var size: String,
    var sizeName: String?,
    var priceIncrease: Long?,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String?,
)

data class AddOnDto(

    var id: UUID? = null,

    @field:NotBlank(message = "AddOn name cannot be null.")
    @field:NotEmpty(message = "AddOn name cannot be empty.")
    var addOnName: String?,

    @field:NotNull(message = "Price cannot be null.")
    @field:Min(value = 1, message = "Price cannot be 0.")
    var price: Long?,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String?
)

data class ComplementDto(

    var id: UUID? = null,

    @field:NotBlank(message = "Complement name cannot be null.")
    @field:NotEmpty(message = "Complement name cannot be empty.")
    var complementName: String?,

    @field:NotNull(message = "Price cannot be null.")
    @field:Min(value = 1, message = "Price cannot be 0.)")
    var price: Long?,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String?
)

