package com.qinet.feastique.model.entity.user

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import com.qinet.feastique.model.entity.discount.Discount
import com.qinet.feastique.model.entity.image.VendorImage
import com.qinet.feastique.model.enums.Region
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Formula
import java.time.LocalTime

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

    var simpleSetup: Boolean = false

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

    @Column(name = "opening_time")
    var openingTime: LocalTime? = null

    @Column(name = "closing_time")
    var closingTime: LocalTime? = null

    @JsonManagedReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var addOn: MutableSet<AddOn> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var address: VendorAddress? = null

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var food: MutableSet<Food> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var vendorPhoneNumber: MutableSet<VendorPhoneNumber> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var complement: MutableSet<Complement> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var discount: MutableSet<Discount> = mutableSetOf()

    @JsonManagedReference
    @OneToMany(
        mappedBy = "vendor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var previewImages: MutableSet<VendorImage> = mutableSetOf()

    @Formula("(SELECT COUNT(vl.id) FROM vendor_likes vl WHERE vl.vendor_id = id)")
    var likeCount: Long = 0

    @Formula("(SELECT COUNT(vb.id) FROM vendor_bookmarks vb WHERE vb.vendor_id = id)")
    var bookmarkCount: Long = 0
}

