package com.qinet.feastique.model.entity.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.FoodOrder
import jakarta.persistence.*


@Entity
@Table(name = "food_order_add_on")
class FoodOrderAddOn {

    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "add_on_id", nullable = false)
    @JsonIgnore
    lateinit var addOn: AddOn

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_order_id", nullable = false)
    @JsonIgnore
    lateinit var foodOrder: FoodOrder
}