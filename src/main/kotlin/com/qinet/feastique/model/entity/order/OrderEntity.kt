package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.Beverage
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@MappedSuperclass
abstract class OrderEntity {
    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

    @Column(name = "food_name")
    @NotBlank(message = "Food name cannot be empty.")
    var foodName: String? = ""

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complement_id")
    @JsonIgnore
    lateinit var complement: Complement

    @JsonBackReference
    @OneToMany(
        mappedBy = "addOn",
        cascade = [CascadeType.ALL],
    )
    @OrderColumn(name = "order_index")
    var addOn: MutableList<AddOn> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "beverage",
        cascade = [CascadeType.ALL],
    )
    @OrderColumn(name = "order_index")
    var beverage: MutableList<Beverage> = mutableListOf()

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_size")
    @JsonIgnore
    lateinit var size: FoodSize
}

