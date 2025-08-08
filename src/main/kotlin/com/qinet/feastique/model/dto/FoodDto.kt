package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class FoodDto(

    @field:NotBlank(message = "Food name cannot be null.")
    @field:NotEmpty(message = "Food name cannot be empty.")
    var foodName: String?,

    @field:NotBlank(message = "Main course cannot be null.")
    @field:NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String?,

    @field:NotBlank(message = "Description cannot be null.")
    @field:NotEmpty(message = "Description cannot be empty.")
    var description: String?,

    @field:NotBlank(message = "Base price cannot be null.")
    @field:NotBlank(message = "Base price cannot be empty.")
    var basePrice: String?,

    @field:NotBlank(message = "Image cannot be null.")
    @field:NotEmpty(message = "Image cannot be empty.")
    var image: String?,

    var foodSize: List<String>,
    var complementIds: List<Long>,
    var addOnIds: List<Long>?
)

