package com.qinet.feastique.model.entity.order.item

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.flavour.BeverageFlavour
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@MappedSuperclass
abstract class BeverageItem : OrderEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_flavour_id", nullable = false)
    @JsonIgnore
    lateinit var beverageFlavour: BeverageFlavour

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_flavour_size_id", nullable = false)
    @JsonIgnore
    lateinit var beverageFlavourSize: BeverageFlavourSize

}

@Entity
@Table(name = "beverage_cart_items")
class BeverageCartItem : BeverageItem() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    var cart: Cart? = null

    @OneToMany(
        mappedBy = "beverageCartItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null
    override fun calculateTotal() : Long {
        val price = (this.beverageFlavourSize.price ?: 0L)
        var total = price.toDouble()

        total *= quantity
        appliedDiscounts.forEach {
            val percentage = it.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }

    fun isSameAs(beverageCartItem: BeverageCartItem): Boolean {
        return this.beverage.id == beverageCartItem.beverage.id &&
                this.beverageFlavour.id == beverageCartItem.beverageFlavour.id &&
                this.beverageFlavourSize.id == beverageCartItem.beverageFlavourSize.id &&
                this.orderType == beverageCartItem.orderType
    }
}


@Entity
@Table(name = "beverage_order_items")
class BeverageOrderItem : BeverageItem(){

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    var order: Order? = null

    @OneToMany(
        mappedBy = "beverageOrderItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()
    override fun calculateTotal() : Long {
        val price = (this.beverageFlavourSize.price ?: 0L)
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

