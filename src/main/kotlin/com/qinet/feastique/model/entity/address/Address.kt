package com.qinet.feastique.model.entity.address

import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

@MappedSuperclass
abstract class Address {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    var country: String = "Cameroon"

    @NotBlank(message = "Region cannot be null.")
    @NotEmpty(message = "Region cannot be empty.")
    var region: String? = ""

    @NotBlank(message = "City cannot be null.")
    @NotEmpty(message = "City cannot be empty.")
    var city: String? = ""

    @NotBlank(message = "Neighbourhood cannot be null.")
    @NotEmpty(message = "Neighbourhood cannot be empty.")
    var neighbourhood: String? = ""

    @Column(name = "street_name")
    var streetName: String? = ""

    @NotBlank(message = "Directions cannot be null.")
    @NotEmpty(message = "Directions cannot be empty.")
    var directions: String? = ""

    var longitude: String? = ""
    var latitude: String? = ""
}

