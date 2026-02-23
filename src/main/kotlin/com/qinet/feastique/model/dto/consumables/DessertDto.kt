package com.qinet.feastique.model.dto.consumables

import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.discount.DiscountDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime
import java.util.*

data class DessertDto(
    var id: UUID? = null,

    @field:NotBlank(message = "Dessert name cannot be empty.")
    var dessertName: String?,

    @field:NotBlank(message = "Description cannot be null.")
    var description: String?,

    @field:NotNull(message = "Dessert type cannot be empty.")
    var dessertType: String? = null,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String?,

    @field:NotNull(message = "Ready-as-from cannot be empty.")
    var readyAsFrom: LocalTime? = null,

    @field:NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int?,

    @field:NotNull(message = "Deliverability must be specified.")
    var deliverable: Boolean?,

    var dailyDeliveryQuantity: Int? = null,
    var deliveryFee: Long?,

    @field:NotNull(message = "Dessert flavours cannot be empty.")
    var dessertFlavours: List<@Valid DessertFlavourDto>,

    @field:NotNull(message = "Dessert order type cannot be empty.")
    var orderTypes: Set<String>,

    @field:NotNull(message = "Dessert availability cannot be empty.")
    var availableDays: Set<String>,

    var dessertImages: Set<@Valid ImageDto>,
    var discounts: Set<@Valid DiscountDto>? = emptySet(),
)

data class DessertFlavourDto(

    var id: UUID? = null,

    @field:NotBlank(message = "Flavour name cannot be empty.")
    var flavourName: String,

    var description: String? = null,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String,

    @field:NotEmpty(message = "Flavour sizes cannot be empty.")
    var flavourSizes: List<@Valid DessertFlavourSizeDto>,

    @field:NotNull(message = "Dessert flavour availability cannot be empty.")
    var availableDays: Set<String>,
)

data class DessertFlavourSizeDto    (
    var id: UUID? = null,

    @field:NotBlank(message = "ConsumableSize cannot be empty.")
    var size: String? = "",

    var sizeName: String?,

    @field:NotNull(message = "Dessert size availability cannot be empty.")
    var availability: String?,

    @field:NotNull(message = "Price is required.")
    @field:Min(value = 0, message = "Price cannot be less than 0.")
    var price: Long? = 0
)

