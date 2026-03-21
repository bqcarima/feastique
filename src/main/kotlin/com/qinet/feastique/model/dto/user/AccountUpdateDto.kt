package com.qinet.feastique.model.dto.user

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate

data class CustomerUpdateDto(
    val username: String,
    val firstName: String,
    val lastName: String,

    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val dob: LocalDate,

    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val anniversary: LocalDate,
    val image: String
)

data class PasswordChangeDto(
    val currentPassword: String,
    val newPassword: String,
    val confirmedNewPassword: String
)

data class VendorUpdateDto(

    @field:NotBlank(message = "Error completing registration. Please try again")
    @field:NotEmpty(message = "Error completing registration. Please try again.")
    var username: String?,

    @field:NotBlank(message = "First name cannot be null.")
    @field:NotEmpty(message = "First name cannot be empty.")
    var firstName: String?,

    @field:NotBlank(message = "Last name cannot be null.")
    @field:NotEmpty(message = "Last name cannot be empty.")
    var lastName: String?,

    @field:NotBlank(message = "Chef name cannot be null.")
    @field:NotEmpty(message = "Chef name cannot be empty.")
    var chefName: String?,

    @field:NotBlank(message = "Restaurant name cannot be null.")
    @field:NotEmpty(message = "Restaurant name cannot be empty.")
    var restaurantName: String?,

    var image: String?,
)

