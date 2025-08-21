package com.qinet.feastique.model.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class FoodDto(

    var id: Long? = null,

    @field:NotBlank(message = "Food name cannot be null.")
    @field:NotEmpty(message = "Food name cannot be empty.")
    var foodName: String?,

    @field:NotBlank(message = "Main course cannot be null.")
    @field:NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String?,

    @field:NotBlank(message = "Description cannot be null.")
    @field:NotEmpty(message = "Description cannot be empty.")
    var description: String?,

    @field:NotNull(message = "Price cannot be null.")
    @field:Min(value = 1, message = "Base price cannot be 0.")
    var basePrice: Long?,
    var foodImage: List<String>,
    var foodSize: List<String>,
    var complementIds: List<Long>,
    var addOnIds: List<Long>?,
    var orderType: List<String>,
    var availability: List<String>,
    var discountIds: List<Long>?,
    var discountActive: Map<Long, Boolean>? = null
)

