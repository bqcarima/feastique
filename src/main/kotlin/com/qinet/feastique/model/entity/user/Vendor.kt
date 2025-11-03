package com.qinet.feastique.model.entity.user

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.enums.Region
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedEntityGraphs
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

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
@Table(name = "vendors")
class Vendor : UserEntity() {

    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Region cannot be empty.")
    var region: Region? = null

    @Column(name = "vendor_code")
    @NotBlank(message = "Vendor code cannot be empty")
    var vendorCode: String? = ""

    @Column(name = "chef_name")
    @NotBlank(message = "Chef name cannot be null.")
    @NotEmpty(message = "Chef name cannot be empty.")
    var chefName: String? = ""

    @Column(name = "restaurant_name")
    var restaurantName: String? = ""

    var balance: Long = 0

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true // Automatic removal of addresses if removed from the vendor
    )
    var addOn: MutableSet<AddOn> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var address: VendorAddress? = null

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var food: MutableSet<Food> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var vendorPhoneNumber: MutableSet<VendorPhoneNumber> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var complement: MutableSet<Complement> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var discount: MutableSet<Discount> = mutableSetOf()
}

