package com.qinet.feastique.model.entity.address

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Region
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

@MappedSuperclass
abstract class Address {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    var country: String = "Cameroon"

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Region cannot be empty.")
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

@Entity
@Table(name = "customer_address")
class CustomerAddress : Address() {

    @Column(name = "is_default")
    var default: Boolean? = false

    @Column(name = "is_active")
    var isActive: Boolean = true

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}

@Entity
@Table(name = "vendor_address")
class VendorAddress : Address() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @OneToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

