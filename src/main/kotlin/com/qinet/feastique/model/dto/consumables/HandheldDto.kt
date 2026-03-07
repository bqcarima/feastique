package com.qinet.feastique.model.dto.consumables

import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.discount.DiscountDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime
import java.util.UUID

data class HandheldDto(

    var id: UUID? = null,

    @field:NotBlank(message = "Handheld name cannot be empty.")
    var handheldName: String?,

    @field:NotBlank(message = "Handheld type cannot be empty.")
    val handheldType: String,

    @field:NotBlank(message = "Description cannot be null.")
    @field:NotEmpty(message = "Description cannot be empty.")
    var description: String?,

    @field:NotNull(message = "Availability status cannot be empty")
    var availability: String,

    @field:NotNull(message = "Deliverability must be specified.")
    var deliverable: Boolean?,

    var readyAsFrom: LocalTime? = null,
    var dailyDeliveryQuantity: Int? = null,
    var preparationTime: Int? = 0,
    var quickDelivery: Boolean? = false,
    val deliveryFee: Long?,

    var fillings: Set<@Valid FillingDto>,
    var handheldSizes: Set<@Valid HandheldSizeDto>,
    var handheldImages: Set<@Valid ImageDto>,
    var orderTypes: Set<String>,
    var availableDays: Set<String>,
    var discounts: Set<@Valid DiscountDto>?
)

data class FillingDto(
    var id: UUID?,
    var name: String?,
    var description: String?,
)

data class HandheldSizeDto(
    var id: UUID?,
    var size: String,
    var sizeName: String?,
    var price: Long?,

    @field:Min(value = 0, message = "Number of fillings cannot be less than 1")
    var numberOfFillings: Long? = 0,

    @field:NotNull(message = "Availability cannot be empty.")
    var availability: String?,
)
