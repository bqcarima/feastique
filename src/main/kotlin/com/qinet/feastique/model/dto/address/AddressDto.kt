package com.qinet.feastique.model.dto.address

import com.qinet.feastique.model.enums.Region
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AddressDto(

    var id: UUID? = null,
    var country: String = "Cameroon",

    @field:NotNull(message = "Region cannot be empty.")
    var region: Region?,

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