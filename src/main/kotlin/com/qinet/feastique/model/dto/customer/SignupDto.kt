package com.qinet.feastique.model.dto.customer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class SignupDto(

    @field:NotBlank(message = "Username cannot be null or empty.")
    var username: String,

    @field:NotBlank(message = "First name cannot be null or empty.")
    var firstName: String,

    @field:NotBlank(message = "Last name cannot be null or empty.")
    var lastName: String,

    @JsonFormat(pattern = "dd-MM-yyyy")
    var dob: LocalDate? = null,

    @JsonFormat(pattern = "dd-MM-yyyy")
    var anniversary: LocalDate? = null,

    @field:NotBlank(message = "Phone number cannot be null or empty.")
    var phoneNumber: String,

    @field:NotBlank(message = "Password cannot be null or empty.")
    var password: String,

    @field:NotBlank(message = "Account type cannot be null or empty.")
    var accountType: String,

    var country: String? = "Cameroon",

    @field:NotBlank(message = "Region cannot be null or empty.")
    var region: String,

    @field:NotBlank(message = "City cannot be null or empty.")
    var city: String,

    @field:NotBlank(message = "Neighbourhood cannot be null or empty.")
    var neighbourhood: String,

    var streetName: String? = null,

    @field:NotBlank(message = "Directions cannot be null or empty.")
    var directions: String,

    var longitude: String? = null,
    var latitude: String? = null
)

