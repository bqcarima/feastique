package com.qinet.feastique.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class BeverageDto(
    var id: UUID? = null,

    @field:NotBlank(message = "Beverage name cannot be empty.")
    var beverageName: String?,

    @field:NotBlank(message = "Beverage group cannot be empty.")
    val beverageGroup: String,

    @field:NotNull(message = "Percentage cannot be null")
    var alcoholic: Boolean?,

    @field:Min(value = 0, message = "Percentage must be at least 1")
    @field:Max(value = 100, message = "Percentage cannot exceed 100")
    val percentage: Int,

    @field:NotNull(message = "Price cannot be null.")
    @field:Min(value = 1, message = "Price cannot be 0.")
    var price: Long?,

    @field:NotNull(message = "Delivery availability must be specified.")
    var delivery: Boolean?
)

