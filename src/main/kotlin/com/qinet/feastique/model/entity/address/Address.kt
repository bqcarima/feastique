package com.qinet.feastique.model.entity.address

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class Address {
    @Id
    @GeneratedValue
    var id: Long? = null

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

