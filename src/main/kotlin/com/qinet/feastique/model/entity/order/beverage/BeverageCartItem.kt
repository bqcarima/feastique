package com.qinet.feastique.model.entity.order.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "beverage_cart_items")
class BeverageCartItem : BeverageEntity() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    lateinit var cart: Cart
    override fun calculateTotal() = (totalAmount ?: 0) * (quantity ?: 0)
}

