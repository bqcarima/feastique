package com.qinet.feastique.model.entity.sales

import com.qinet.feastique.model.entity.provisions.addOn.AddOn
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "add_on_sales")
class AddOnSale : BaseRecord() {

    @ManyToOne
    @JoinColumn
    lateinit var addOn: AddOn

    @ManyToOne
    @JoinColumn(name = "food_order_item_id")
    lateinit var foodOrderItem: FoodOrderItem
}

