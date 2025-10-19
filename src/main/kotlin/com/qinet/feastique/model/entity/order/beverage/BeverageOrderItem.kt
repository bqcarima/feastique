package com.qinet.feastique.model.entity.order.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.Order
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "beverage_order_items")
class BeverageOrderItem : BeverageEntity(){

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    lateinit var order: Order
    override fun calculateTotal() = (beverage.price ?: 0) * (quantity ?: 0)
}

