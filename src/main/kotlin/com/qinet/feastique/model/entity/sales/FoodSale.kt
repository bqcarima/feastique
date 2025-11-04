package com.qinet.feastique.model.entity.sales

import com.qinet.feastique.model.entity.provisions.food.Food
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "food_sales")
class FoodSale : BaseRecord (){

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    lateinit var food: Food

    @ManyToOne
    @JoinColumn(name = "food_order_item_id", nullable = false)
    lateinit var foodOrderItem: FoodOrderItem
}

