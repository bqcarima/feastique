package com.qinet.feastique.model.entity.user

//import com.qinet.feastique.model.entity.review.FoodReview
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import jakarta.persistence.*
import java.time.LocalDate

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
class Customer : UserEntity() {

    @Column(nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
    var dob: LocalDate? = null

    @Column(nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
    var anniversary: LocalDate? = null

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

    // Food order relationship
    @JsonManagedReference
    @OneToMany(
        mappedBy = "customer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodOrder: MutableList<FoodOrder> = mutableListOf()
}

