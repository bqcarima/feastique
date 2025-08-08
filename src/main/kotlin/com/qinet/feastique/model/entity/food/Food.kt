package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.Vendor
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.FoodDiscount
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "food")
class Food {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "food_name")
    @NotBlank(message = "Food name cannot be null.")
    @NotEmpty(message = "Food name cannot be empty.")
    var foodName: String? = ""

    @Column(name = "main_course")
    @NotBlank(message = "Main course cannot be null.")
    @NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String? = ""

    @NotBlank(message = "Description cannot be null.")
    @NotEmpty(message = "Description cannot be empty.")
    var description: String? = ""

    @Column(name = "base_price")
    @NotBlank(message = "Base price cannot be null.")
    @NotBlank(message = "Base price cannot be empty.")
    var basePrice: String? = ""

    @NotBlank(message = "Image cannot be null.")
    @NotEmpty(message = "Image cannot be empty.")
    var image: String? = ""

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAddOn: MutableSet<FoodAddOn> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAvailability: MutableSet<FoodAvailability> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodComplement: MutableSet<FoodComplement> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodDiscount: MutableSet<FoodDiscount> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodOrderType: MutableSet<FoodOrderType> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodSize: MutableSet<FoodSize> = mutableSetOf()
}

