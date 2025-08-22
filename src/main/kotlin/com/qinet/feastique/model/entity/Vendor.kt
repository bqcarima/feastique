package com.qinet.feastique.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.entity.post.Post
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import java.util.*

@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Vendor.withAddressAndPhoneNumbers",
            attributeNodes = [
                NamedAttributeNode("address"),
                NamedAttributeNode("vendorPhoneNumber")
            ]
        ),
        NamedEntityGraph(
            name = "Vendor.withFoodAndDiscounts",
            attributeNodes = [
                NamedAttributeNode("food"),
                NamedAttributeNode("discount")
            ]
        ),
        NamedEntityGraph(
            name = "Vendor.withAllRelations",
            attributeNodes = [
                NamedAttributeNode("address"),
                NamedAttributeNode("vendorPhoneNumber"),
                NamedAttributeNode("food"),
                NamedAttributeNode("discount"),
                NamedAttributeNode("addOn"),
                NamedAttributeNode("complement")
            ]
        )
    ]
)

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

    @NotBlank(message = "Password cannot be null.")
    @NotEmpty(message = "Password cannot be empty.")
    var accountType: String? = ""

    @Column(name = "registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @CreationTimestamp
    var registrationDate: Date? = null

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true // Automatic removal of addresses if removed from the vendor
    )
    var addOn: MutableList<AddOn> = mutableListOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var address: VendorAddress? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var complement: MutableList<Complement> = mutableListOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var discount: MutableList<Discount> = mutableListOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var food: MutableList<Food> = mutableListOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var vendorPhoneNumber: MutableList<VendorPhoneNumber> = mutableListOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var post: MutableList<Post> = mutableListOf()
}

