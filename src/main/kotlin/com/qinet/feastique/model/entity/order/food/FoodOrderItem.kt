package com.qinet.feastique.model.entity.order.food

import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Order
import jakarta.persistence.*

// @Suppress("JpaEntityGraphsInspection")
@Entity
@Table(name = "food_order_items")
/*@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "FoodOrder.withAllRelations",
            attributeNodes = [
                NamedAttributeNode("order"),
                NamedAttributeNode("food"),
                NamedAttributeNode("complement"),
                NamedAttributeNode("size"),
                NamedAttributeNode("addOns", subgraph = "addOn-subgraph"),
                NamedAttributeNode("appliedDiscounts", subgraph = "appliedDiscount-subgraph"),
            ],
            subgraphs = [
                NamedSubgraph(
                    name = "addOn-subgraph",
                    attributeNodes = [NamedAttributeNode("addOn")]
                ),
                NamedSubgraph(
                    name = "appliedDiscount-subgraph",
                    attributeNodes = [NamedAttributeNode("beverage")]
                )
            ]
        )
    ]
)*/
class FoodOrderItem : FoodEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    lateinit var order: Order

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
        orphanRemoval = false
    )
    @OrderColumn(name = "order_index")
    var appliedDiscounts: MutableList<AppliedDiscount> = mutableListOf()

    override fun calculateTotal(): Long {
        val basePrice = (food.basePrice ?: 0L) + (size.priceIncrease ?: 0L)
        var total = basePrice.toDouble()

        total += complement.price ?: 0
        total += addOns.sumOf { it.price ?: 0L }
        total *= quantity ?: 1
        appliedDiscounts.forEach { applied ->
            val percentage = applied.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }
}

