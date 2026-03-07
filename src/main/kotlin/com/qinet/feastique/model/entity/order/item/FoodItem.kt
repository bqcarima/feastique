package com.qinet.feastique.model.entity.order.item

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.consumables.complement.Complement
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import jakarta.persistence.*
import java.time.LocalDateTime
import kotlin.collections.forEach

@MappedSuperclass
abstract class FoodItem : OrderEntity() {

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

@Entity
@Table(name = "food_cart_items")
class FoodCartItem : FoodItem() {

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

    fun isSameAs(foodCartItem: FoodCartItem): Boolean {
        return this.food.id == foodCartItem.food.id &&
                this.size.id == foodCartItem.size.id &&
                this.complement.id == foodCartItem.complement.id &&
                this.addOns.map { it.id }.toSet() == foodCartItem.addOns.map { it.id }.toSet() &&
                this.orderType == foodCartItem.orderType
    }

}

@Entity
@Table(name = "food_order_items")
class FoodOrderItem : FoodItem() {

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

