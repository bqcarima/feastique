package com.qinet.feastique.model.entity.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.FoodCart
import com.qinet.feastique.model.entity.order.FoodOrder
import jakarta.persistence.*


@Entity
@Table(name = "food_order_add_on")
class OrderAddOn {

    @Id
    @GeneratedValue
    var id: Long? = null

    // Food order relationships
    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "add_on_id", nullable = false)
    @JsonIgnore
    lateinit var addOn: AddOn

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_order_id", nullable = true)
    @JsonIgnore
    lateinit var foodOrder: FoodOrder

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    @JsonIgnore
    lateinit var foodCart: FoodCart
}