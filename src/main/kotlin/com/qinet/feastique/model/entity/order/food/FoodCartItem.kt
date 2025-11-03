package com.qinet.feastique.model.entity.order.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import jakarta.persistence.*

@Entity
@Table(name = "food_cart_items")
class FoodCartItem : FoodEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    var cart: Cart? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "food_cart_item_addons",
        joinColumns = [JoinColumn(name = "food_cart_item_id")],
        inverseJoinColumns = [JoinColumn(name = "addon_id")]
    )
    var addOns: MutableSet<AddOn> = mutableSetOf()

    @OneToMany(
        mappedBy = "foodCartItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    override fun calculateTotal(): Long {
        val basePrice = (food.basePrice ?: 0L) + (size.priceIncrease ?: 0L)
        var total = basePrice.toDouble()

        total += complement.price ?: 0
        total += addOns.sumOf { it.price ?: 0L }
        total *= quantity
        appliedDiscounts.forEach {
            val percentage = it.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }
}

