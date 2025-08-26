package com.qinet.feastique.model.dto.vendor

import com.qinet.feastique.model.enums.AccountType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class VendorSignupDto(

    // Information meant for the vendor table
    @field:NotBlank(message = "Error completing registration. Please try again.")
    var username: String?,

    @field:NotBlank(message = "First name cannot be empty.")
    var firstName: String?,

    @field:NotBlank(message = "Last name cannot be empty.")
    var lastName: String?,

    @field:NotBlank(message = "Chef name cannot be empty.")
    var chefName: String?,

    var restaurantName: String?,

    @field:NotBlank(message = "Password cannot be empty.")
    var password: String?,

    var balance: Long?,
    var verified: Boolean?,
    var image: String?,

    @field:NotNull(message = "Account type cannot be null.")
    var accountType: AccountType?,

    // Information meant for the vendor phone number table
    @field:NotBlank(message = "Phone number cannot be empty.")
    var phoneNumber: String?,

    // information meant for the address table
    @field:NotBlank(message = "Region cannot be empty.")
    var region: String?,

    @field:NotBlank(message = "City cannot be empty.")
    var city: String?,

    @field:NotBlank(message = "Neighbourhood cannot be empty.")
    var neighbourhood: String?,

    var streetName: String?,

    @field:NotEmpty(message = "Directions cannot be empty.")
    var directions: String?,

    var longitude: String?,
    var latitude: String?
)