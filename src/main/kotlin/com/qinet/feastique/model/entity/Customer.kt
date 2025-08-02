package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.enums.AccountType
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table(name ="customer")
class Customer {

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

    var dob: Date? = null

    @NotBlank(message = "Password cannot be null.")
    @NotEmpty(message = "Password cannot be empty.")
    var password: String? = ""

    var anniversary: Date? = null
    var verified: Boolean? = false
    var image: String? = ""

    @Column(name  = "account_type" ,nullable = false)
    @Enumerated(EnumType.STRING)
    var accountType: AccountType? = null

    @Column(name = "registration_date")
    @CreationTimestamp
    var registrationDate: Date? = null

    @Column(name = "account_updated_date")
    @UpdateTimestamp
    var accountUpdated: Date? = null

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "customer",
        orphanRemoval = true // Automatic removal of addresses if removed from the customer
    )
    var addresses: MutableSet<CustomerAddress> = mutableSetOf()
}
