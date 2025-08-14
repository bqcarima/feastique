package com.qinet.feastique.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class DiscountDto(
    var id: Long? = null,

    @field:NotBlank(message = "Discount name cannot be blank")
    val discountName: String,

    @field:NotNull(message = "Percentage cannot be null")
    @field:Min(value = 1, message = "Percentage must be at least 1")
    @field:Max(value = 100, message = "Percentage cannot exceed 100")
    val percentage: Int,

    @field:NotNull(message = "Start date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val startDate: Date,

    @field:NotNull(message = "End date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val endDate: Date

)
