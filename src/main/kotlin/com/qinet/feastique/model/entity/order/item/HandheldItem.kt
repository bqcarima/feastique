package com.qinet.feastique.model.entity.order.item

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.consumables.filling.Filling
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import com.qinet.feastique.model.entity.discount.AppliedDiscount
import com.qinet.feastique.model.entity.order.Cart
import com.qinet.feastique.model.entity.order.Order
import com.qinet.feastique.model.entity.order.OrderEntity
import com.qinet.feastique.model.entity.size.HandheldSize
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@MappedSuperclass
abstract class HandheldItem : OrderEntity() {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handheld_size_id", nullable = false)
    @JsonIgnore
    lateinit var size: HandheldSize
}

@Entity
@Table(name = "handheld_cart_items")
class HandheldCartItem: HandheldItem() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    var cart: Cart? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "handheld_cart_item_fillings",
        joinColumns = [JoinColumn(name = "handheld_cart_item_id")],
        inverseJoinColumns = [JoinColumn(name = "filling_id")]
    )
    var fillings: MutableSet<Filling> = mutableSetOf()

    @OneToMany(
        mappedBy = "handheldCartItem",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null

    override fun calculateTotal(): Long {
        val price = (this.size.price ?: 0L)
        var total = price.toDouble()

        total *= quantity
        appliedDiscounts.forEach {
            val percentage = it.discount.percentage ?: 0
            total *= (1 - (percentage / 100.0))
        }

        totalAmount = total.toLong()
        return totalAmount!!
    }

    fun isSameAs(handheldCartItem: HandheldCartItem): Boolean {
        return this.handheld.id == handheldCartItem.handheld.id &&
                this.size.id == handheldCartItem.size.id &&
                this.orderType == handheldCartItem.orderType
    }
}

@Entity
@Table(name = "handheld_order_items")
class HandheldOrderItem : HandheldItem() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    var order: Order? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "handheld_order_item_fillings",
        joinColumns = [JoinColumn(name = "handheld_order_item_id")],
        inverseJoinColumns = [JoinColumn(name = "filling_id")]
    )
    var fillings: MutableSet<Filling> = mutableSetOf()

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var appliedDiscounts: MutableSet<AppliedDiscount> = mutableSetOf()

    @Column(name = "added_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    override var addedAt: LocalDateTime? = null

    override fun calculateTotal(): Long {
        val price = (this.size.price ?: 0L)
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

