package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class VendorSignupDto(

    // Information meant for the vendor table
    @field:NotBlank(message = "Error completing registration. Please try again")
    @field:NotEmpty(message = "Error completing registration. Please try again.")
    var username: String?,

    @field:NotBlank(message = "First name cannot be null.")
    @field:NotEmpty(message = "First name cannot be empty.")
    var firstName: String?,

    @field:NotBlank(message = "Last name cannot be null.")
    @field:NotEmpty(message = "Last name cannot be empty.")
    var lastName: String?,

    @field:NotBlank(message = "Phone number cannot be null.")
    @field:NotEmpty(message = "Phone number cannot be empty.")
    var defaultPhoneNumber: String?,

    @field:NotBlank(message = "Chef name cannot be null.")
    @field:NotEmpty(message = "Chef name cannot be empty.")
    var chefName: String?,

    var restaurantName: String?,

    @field:NotBlank(message = "Password cannot be null.")
    @field:NotEmpty(message = "Password cannot be empty.")
    var password: String?,

    var balance: Long?,
    var verified: Boolean?,
    var image: String?,

    @field:NotBlank(message = "Account type cannot be null.")
    @field:NotEmpty(message = "Account type cannot be empty.")
    var accountType: String?,


    // information meant for the address table
    @field:NotBlank(message = "Region cannot be null.")
    @field:NotEmpty(message = "Region cannot be empty.")
    var region: String?,

    @field:NotBlank(message = "City cannot be null.")
    @field:NotEmpty(message = "City cannot be empty.")
    var city: String?,

    @field:NotBlank(message = "Neighbourhood cannot be null.")
    @field:NotEmpty(message = "Neighbourhood cannot be empty.")
    var neighbourhood: String?,

    var streetName: String?,

    @field:NotBlank(message = "Directions cannot be null.")
    @field:NotEmpty(message = "Directions cannot be empty.")
    var directions: String?,

    var longitude: String?,
    var latitude: String?
)
