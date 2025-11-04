package com.qinet.feastique.model.entity.sales

import com.qinet.feastique.model.entity.provisions.complement.Complement
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "complement_sales")
class ComplementSale : BaseRecord() {

    @ManyToOne
    @JoinColumn(name = "complement_id", nullable = false)
    lateinit var complement: Complement

    @ManyToOne
    @JoinColumn(name = "food_order_item_id")
    lateinit var foodOrderItem: FoodOrderItem
}

