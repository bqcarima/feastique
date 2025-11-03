package com.qinet.feastique.model.entity.order.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Order
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "food_order_items")
class FoodOrderItem : FoodEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    var order: Order? = null

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "food_order_item_addons",
        joinColumns = [JoinColumn(name = "food_order_item_id")],
        inverseJoinColumns = [JoinColumn(name = "addon_id")]
    )
    @OrderColumn(name = "addon_index")
    var addOns: MutableList<AddOn> = mutableListOf()

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var appliedDiscounts: MutableList<AppliedDiscount> = mutableListOf()

    override fun calculateTotal(): Long {
        val basePrice = (food.basePrice ?: 0L) + (size.priceIncrease ?: 0L)
        var total = basePrice.toDouble()

        total += complement.price ?: 0
        total += addOns.sumOf { it.price ?: 0L }
        total *= quantity
        appliedDiscounts.forEach { applied ->
            val percentage = applied.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }
}

