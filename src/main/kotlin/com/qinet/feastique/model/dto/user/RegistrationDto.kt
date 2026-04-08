package com.qinet.feastique.model.dto.user

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.qinet.feastique.common.validator.password.ValidPassword
import com.qinet.feastique.common.validator.password.ValidPin
import com.qinet.feastique.common.validator.phoneNumber.ValidPhoneNumber
import com.qinet.feastique.common.validator.username.ValidUsername
import com.qinet.feastique.model.enums.AccountType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomerSignupDto(

    @field:NotBlank(message = "Username cannot be null or empty.")
    var username: String,

    @field:NotBlank(message = "First name cannot be null or empty.")
    var firstName: String,

    @field:NotBlank(message = "Last name cannot be null or empty.")
    var lastName: String,

    @JsonFormat(pattern = "dd-MM-yyyy")
    var anniversary: LocalDate? = null,

    @field:ValidPhoneNumber(message = "Phone number must start with 6 and be exactly 9 digits long.")
    var phoneNumber: String,

    @field:ValidPin(message = "PIN must be exactly characters long.")
    var password: String,

    var country: String? = "Cameroon",

    @field:NotNull(message = "Region cannot be null or empty.")
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


data class VendorSignupDto(

    // Information meant for the vendor table
    @field:NotBlank(message = "Error completing registration. Please try again.")
    @ValidUsername
    var username: String?,

    @field:NotBlank(message = "First name cannot be empty.")
    var firstName: String?,

    @field:NotBlank(message = "Last name cannot be empty.")
    var lastName: String?,

    @field:NotBlank(message = "Chef name cannot be empty.")
    var chefName: String?,

    var restaurantName: String?,

    @field:ValidPassword
    var password: String?,

    var balance: Long?,

    @field:NotNull(message = "Opening time cannot be empty.")
    var openingTime: LocalTime?,

    @field:NotNull(message = "Closing time cannot be empty.")
    var closingTime: LocalTime?,

    var verified: Boolean?,
    var image: String?,

    @field:NotNull(message = "Account type cannot be null.")
    var accountType: AccountType?,

    // Information meant for the vendor phone number table
    @field:ValidPhoneNumber(message = "Phone number must start with 6 and be exactly 9 digits long.")
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