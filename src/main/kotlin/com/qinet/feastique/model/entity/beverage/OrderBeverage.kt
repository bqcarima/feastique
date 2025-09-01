package com.qinet.feastique.model.entity.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.FoodCart
import com.qinet.feastique.model.entity.order.FoodOrder
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "food_order_beverage")
class OrderBeverage {
    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id")
    @JsonIgnore
    lateinit var beverage: Beverage

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