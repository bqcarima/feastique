package com.qinet.feastique.model.dto.customer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonFormat
import com.qinet.feastique.common.validator.password.ValidPassword
import com.qinet.feastique.common.validator.phoneNumber.ValidPhoneNumber
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
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

    @field:ValidPhoneNumber(message = "Phone number must start with 6 and be exactly 9 digits long.")
    var phoneNumber: String,

    @field:ValidPassword(message = "Password must be at least 8 characters long, contain at least one uppercase letter and one number.")
    var password: String,

    @field:NotNull(message = "Account type cannot be null or empty.")
    var accountType: AccountType,

    var country: String? = "Cameroon",

    @field:NotNull(message = "Region cannot be null or empty.")
    var region: Region,

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

