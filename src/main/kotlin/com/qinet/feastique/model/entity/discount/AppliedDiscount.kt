package com.qinet.feastique.model.entity.discount

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.food.FoodCartItem
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "applied_discounts")
class AppliedDiscount {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @ManyToOne
    @JoinColumn(name = "discount_id")
    lateinit var discount: Discount

    @ManyToOne
    @JoinColumn(name = "food_order_item_id")
    var foodOrderItem: FoodOrderItem? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_cart_item_id")
    @JsonIgnore
    var foodCartItem: FoodCartItem? = null
}

