package com.qinet.feastique.model.entity.order.item

import com.fasterxml.jackson.annotation.*
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.flavour.DessertFlavour
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import jakarta.persistence.*
import jakarta.persistence.Table
import java.time.LocalDateTime

@MappedSuperclass
abstract class DessertItem : OrderEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_flavour_id", nullable = false)
    @JsonIgnore
    lateinit var dessertFlavour: DessertFlavour

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_flavour_size", nullable = false)
    @JsonIgnore
    lateinit var dessertFlavourSize: DessertFlavourSize
}

@Entity
@Table(name = "dessert_cart_items")
class DessertCartItem : DessertItem() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    var cart: Cart? = null

    @OneToMany(
        mappedBy = "dessertCartItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null

    override fun calculateTotal() : Long {
        val price = (this.dessertFlavourSize.price ?: 0L)
        var total = price.toDouble()

        total *= quantity
        appliedDiscounts.forEach {
            val percentage = it.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }

    fun isSameAs(dessertCartItem: DessertCartItem): Boolean {
        return this.dessert.id == dessertCartItem.dessert.id &&
                this.dessertFlavour.id == dessertCartItem.dessertFlavour.id &&
                this.dessertFlavourSize.id == dessertCartItem.dessertFlavourSize.id &&
                this.orderType == dessertCartItem.orderType
    }
}

@Entity
@Table(name = "dessert_order_items")
class DessertOrderItem : DessertItem() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    var order: Order? = null

    @OneToMany(
        mappedBy = "dessertOrderItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null
    override fun calculateTotal() : Long {
        val price = (this.dessertFlavourSize.price ?: 0L)
        var total = price.toDouble()

        total *= quantity
        appliedDiscounts.forEach {
            val percentage = it.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }
}


