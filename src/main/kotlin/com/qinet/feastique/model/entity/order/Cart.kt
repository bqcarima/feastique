package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.item.BeverageCartItem
import com.qinet.feastique.model.entity.order.item.DessertCartItem
import com.qinet.feastique.model.entity.order.item.FoodCartItem
import com.qinet.feastique.model.entity.order.item.HandheldCartItem
import com.qinet.feastique.model.entity.user.Customer
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "carts")
class Cart {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    @JsonIgnore
    var customer: Customer? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodCartItems: MutableList<FoodCartItem> = Collections.synchronizedList(mutableListOf())

    @JsonBackReference
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var beverageCartItems: MutableList<BeverageCartItem> = Collections.synchronizedList(mutableListOf())

    @JsonBackReference
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var dessertCartItems: MutableList<DessertCartItem> = Collections.synchronizedList(mutableListOf())

    @JsonBackReference
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var handheldCartItems: MutableList<HandheldCartItem> = Collections.synchronizedList(mutableListOf())



    // Lazy cached combined list
    @Transient
    @JsonIgnore
    internal var itemsCache: MutableList<OrderEntity>? = null

    @get:JsonProperty("items") //Transient
    val items: List<OrderEntity>
        get() {
            if (itemsCache == null) {
                //Build cache lazily on first access
                itemsCache =
                    (foodCartItems + beverageCartItems + dessertCartItems + handheldCartItems).sortedBy { it.addedAt }.toMutableList()
            }
            return itemsCache!!
        }

    @Column(name = "total_amount")
    var totalAmount: Long? = null

    fun addItem(item: OrderEntity) = synchronized(this) {
        when (item) {
            is FoodCartItem -> foodCartItems.add(item.apply { cart = this@Cart })
            is BeverageCartItem -> beverageCartItems.add(item.apply { cart = this@Cart })
            is DessertCartItem -> dessertCartItems.add(item.apply { cart = this@Cart })
            is HandheldCartItem -> handheldCartItems.add((item.apply { cart = this@Cart }))
        }

        // Insert while maintaining order
        itemsCache?.let { cache ->
            val index = cache.indexOfFirst { it.addedAt!! > item.addedAt }
            if (index >= 0) cache.add(index, item) else cache.add(item)
        }
    }

    fun removeItem(item: OrderEntity) = synchronized(this) {
        when (item) {
            is FoodCartItem -> {
                item.cart = null
                foodCartItems.remove(item)
            }

            is BeverageCartItem -> {
                item.cart = null
                beverageCartItems.remove(item)
            }

            is DessertCartItem -> {
                item.cart = null
                dessertCartItems.remove(item)
            }

            is HandheldCartItem -> {
                item.cart = null
                handheldCartItems.remove(item)
            }
        }
        itemsCache?.remove(item)
    }

    fun removeItems(itemsList: List<UUID>): Unit = synchronized(this) {
        // stop if the list is empty
        if (itemsList.isEmpty()) return

        // converted to Set to increase performance
        val items = itemsList.toSet()
        foodCartItems.removeIf { item ->
            if (item.id in items) {
                item.cart = null
                true
            } else false
        }

        beverageCartItems.removeIf { item ->
            if (item.id in items) {
                item.cart = null
                true
            } else false
        }

        dessertCartItems.removeIf { item ->
            if (item.id in items) {
                item.cart = null
                true
            } else false
        }

        handheldCartItems.removeIf { item ->
            if (item.id in items) {
                item.cart = null
                true
            } else false
        }
        itemsCache?.removeIf { it.id in items }
    }

    fun calculateTotal(): Long =
        foodCartItems.sumOf { it.totalAmount ?: 0 } +
                beverageCartItems.sumOf { it.totalAmount ?: 0 } +
                        dessertCartItems.sumOf { it.totalAmount  ?: 0 } +
                                handheldCartItems.sumOf { it.totalAmount ?: 0 }
}

