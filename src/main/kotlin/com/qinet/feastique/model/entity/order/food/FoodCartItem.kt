package com.qinet.feastique.model.entity.order.food

import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import jakarta.persistence.*

@Entity
@Table(name = "food_cart_items")
class FoodCartItem : FoodEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    lateinit var cart: Cart

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    @OrderColumn(name = "order_index")
    var addOns: MutableList<AddOn> = mutableListOf()

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    @OrderColumn(name = "order_index")
    var appliedDiscounts: MutableList<AppliedDiscount> = mutableListOf()

    override fun calculateTotal() = (totalAmount ?: 0) * (quantity ?: 0)
}

