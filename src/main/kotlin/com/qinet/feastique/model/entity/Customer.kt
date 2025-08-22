package com.qinet.feastique.model.entity

//import com.qinet.feastique.model.entity.review.FoodReview
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import com.qinet.feastique.model.enums.AccountType
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "customer")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Customer.withPhoneNumberAndAddress",
            attributeNodes = [
                NamedAttributeNode("phoneNumber"),
                NamedAttributeNode("address")
            ]
        )
    ]
)
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

    @Column(nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
    var dob: LocalDate? = null

    @NotBlank(message = "Password cannot be null.")
    @NotEmpty(message = "Password cannot be empty.")
    var password: String? = ""

    @Column(nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
    var anniversary: LocalDate? = null
    var verified: Boolean? = false
    var image: String? = ""

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var accountType: AccountType? = null

    @Column(name = "registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @CreationTimestamp
    var registrationDate: Date? = null

    @Column(name = "account_updated_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @UpdateTimestamp
    var accountUpdated: Date? = null

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "customer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true // Automatic removal of addresses if removed from the customer
    )
    var phoneNumber: MutableSet<CustomerPhoneNumber> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "customer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var address: MutableSet<CustomerAddress> = mutableSetOf()
}

