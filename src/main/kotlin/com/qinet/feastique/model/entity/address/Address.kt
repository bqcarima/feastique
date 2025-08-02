package com.qinet.feastique.model.entity.address

import com.qinet.feastique.model.enums.Region
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class Address {
    @Id
    @GeneratedValue
    var id: Long? = null

    var country: String = "Cameroon"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var region: Region? = null

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