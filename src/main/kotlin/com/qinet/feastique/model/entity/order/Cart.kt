package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.entity.user.Customer
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "carts")
class Cart {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    lateinit var customer: Customer

    @OneToMany(
        // mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var items: MutableSet<OrderEntity> = mutableSetOf()

    fun removeItem(id: UUID) {
        val item = items.find { it.id == id}
            .takeIf { true }
            ?: throw RequestedEntityNotFoundException("Item not found in cart.")
        items.remove(item)
    }

    fun clearCart() {
        items.clear()
    }

    fun totalPrice(): Long = items.sumOf { it.calculateTotal() }

    // Group cart items by vendor
    // fun ordersByVendor(): Map<Vendor, List<OrderEntity>> = items.groupBy { it.vendor }
}

