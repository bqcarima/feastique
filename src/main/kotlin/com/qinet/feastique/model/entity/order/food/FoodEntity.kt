package com.qinet.feastique.model.entity.order.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.provisions.complement.Complement
import com.qinet.feastique.model.entity.provisions.food.Food
import com.qinet.feastique.model.entity.provisions.food.FoodSize
import com.qinet.feastique.model.entity.order.OrderEntity
import jakarta.persistence.*

@MappedSuperclass
abstract class FoodEntity : OrderEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complement_id", nullable = false)
    @JsonIgnore
    lateinit var complement: Complement

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_size_id", nullable = false)
    @JsonIgnore
    lateinit var size: FoodSize
}

