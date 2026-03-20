package com.qinet.feastique.model.dto.consumables

import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.discount.DiscountDto
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalTime
import java.util.UUID

data class BeverageDto(
    var id: UUID? = null,

    @field:NotBlank(message = "Beverage name cannot be empty.")
    var beverageName: String?,

    @field:NotBlank(message = "Beverage group cannot be empty.")
    val beverageGroup: String,

    @field:NotNull(message = "Percentage cannot be null")
    var alcoholic: Boolean?,

    @field:Min(value = 0, message = "Percentage cannot be less than 0")
    @field:Max(value = 100, message = "Percentage cannot exceed 100")
    val percentage: Int,

    var orderTypes: Set<String>,

    @field:NotNull(message = "Beverage availability cannot be empty.")
    var availableDays: Set<String>,

    @field:Min(value = 0, message = "Price cannot be less than 0.")
    val deliveryFee: Long?,

    @field:NotNull(message = "Deliverability must be specified.")
    var deliverable: Boolean?,

    var dailyDeliveryQuantity: Int? = null,

    @field:NotNull(message = "Availability status cannot be empty")
    var availability: String,

    var readyAsFrom: LocalTime? = null,

    @field:NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int?,

    var quickDelivery: Boolean? = false,

    @field:NotNull(message = "Beverage flavours cannot be empty.")
    var beverageFlavours: Set<@Valid BeverageFlavourDto>,

    @field:NotNull(message = "Image ur cannot be empty.")
    var beverageImages: Set<@Valid ImageDto>,

    var discounts: Set<@Valid DiscountDto>? = mutableSetOf()
)

data class BeverageFlavourDto(
    var id: UUID? = null,

    @field:NotBlank(message = "Flavour name cannot be empty.")
    var flavourName: String,

    var description: String? = null,

    @field:NotEmpty(message = "Flavour sizes cannot be empty.")
    var flavourSizes: Set<@Valid BeverageFlavourSizeDto>,

    @field:NotNull(message = "Availability status cannot be empty")
    var availability: String,
)

data class BeverageFlavourSizeDto(
    var id: UUID? = null,

    @field:NotNull(message = "Size cannot be empty")
    var size: String? = null,

    @field:NotBlank(message = "ConsumableSize cannot be empty.")
    var sizeName: String? = "",

    @field:NotNull(message = "Price is required.")
    @field:Min(value = 0, message = "Price cannot be less than 0.")
    var price: Long? = 0,

    @field:NotNull(message = "Availability status cannot be empty")
    var availability: String,
)

