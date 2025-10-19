package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.*

data class AddressDto(

    var id: UUID? = null,
    var country: String = "Cameroon",

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
    var latitude: String?,

    var default: Boolean? = false,
)

