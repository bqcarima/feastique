package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.enums.AccountType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import java.util.Date
import java.util.UUID

@Entity
@Table(name = "vendor")
class Vendor {

    @Id
    @GeneratedValue
    var id: Long? = null

    @NotBlank(message = "Username cannot be null.")
    @NotEmpty(message = "Username cannot be empty.")
    var username: String = UUID.randomUUID().toString()

    @Column(name = "first_name")
    @NotBlank(message = "First name cannot be null.")
    @NotEmpty(message = "First name cannot be empty.")
    var firstName: String? = ""

    @Column(name = "last_name")
    @NotBlank(message = "Last name cannot be null.")
    @NotEmpty(message = "Last name cannot be empty.")
    var lastName: String? = ""

    @Column(name = "default_phone_number")
    @NotBlank(message = "Phone number cannot be null.")
    @NotEmpty(message = "Phone number cannot be empty.")
    var defaultPhoneNumber: String? = ""

    @Column(name = "chef_name")
    @NotBlank(message = "Chef name cannot be null.")
    @NotEmpty(message = "Chef name cannot be empty.")
    var chefName: String? = ""

    @Column(name = "restaurant_name")
    var restaurantName: String? = ""

    @NotBlank(message = "Password cannot be null.")
    @NotEmpty(message = "Password cannot be empty.")
    var password: String? = ""

    var balance: Long = 0
    var verified: Boolean = false
    var image: String? = ""

    @Column(name  = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var accountType: AccountType? = null

    @Column(name = "registration_date")
    @CreationTimestamp
    var registrationDate: Date? = null

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "vendor",
        orphanRemoval = true // Automatic removal of addresses if removed from the customer
    )
    var addresses: MutableSet<VendorAddress> = mutableSetOf()
}