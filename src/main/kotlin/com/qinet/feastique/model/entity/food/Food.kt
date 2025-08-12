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
    var basePrice: Long? = 0

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
    @OrderColumn(name = "order_index")
    var foodImage: MutableList<FoodImage> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodAvailability: MutableList<FoodAvailability> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodComplement: MutableList<FoodComplement> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodDiscount: MutableList<FoodDiscount> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodOrderType: MutableList<FoodOrderType> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodSize: MutableList<FoodSize> = mutableListOf()
}

