package com.qinet.feastique.model.entity.discount

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.food.Food
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "food_discounts")
class FoodDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    lateinit var discount: Discount

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

}

