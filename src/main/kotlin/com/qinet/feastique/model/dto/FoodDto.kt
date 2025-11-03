package com.qinet.feastique.model.dto

import com.qinet.feastique.model.enums.Size
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime
import java.util.UUID

data class FoodDto(

    var id: UUID? = null,

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

    @field:NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int?,

    var deliveryTime: LocalTime?,
    var deliveryFee: Long?,
    var foodImage: List<String>,
    var foodSizeMap: Map<Size, Long>,
    var foodSizeName: List<String>?,
    var complementIds: List<UUID>,
    var addOnIds: List<UUID>?,
    var orderType: List<String>,
    var availability: List<String>,
    var discountIds: List<UUID>?,
)

