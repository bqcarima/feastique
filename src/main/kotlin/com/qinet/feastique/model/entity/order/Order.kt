package com.qinet.feastique.model.entity.order

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.order.beverage.BeverageOrderItem
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.math.roundToLong

//@Suppress("JpaEntityGraphsInspection")
/*@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Order.withAllRelations",
            attributeNodes = [
                NamedAttributeNode("userOrderCode"),
                NamedAttributeNode("customerAddress"),
                NamedAttributeNode("orderStatus"),
                NamedAttributeNode("placementTime"),
                NamedAttributeNode("deliveryTime"),
                NamedAttributeNode("completedTime"),
                NamedAttributeNode("deliveryFee"),
                NamedAttributeNode(
                    value = "vendor",
                    subgraph = "Vendor.basic"
                ),
                NamedAttributeNode(
                    value = "items",
                    subgraph = "OrderItems.full"
                )
            ],
            subgraphs = [

                // level 2 relationships
                // Vendor info
                NamedSubgraph(
                    name = "Vendor.basic",
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("chefName"),
                        NamedAttributeNode("restaurantName")
                    ]
                ),

                // Food order info
                NamedSubgraph(
                    name = "FoodOrderItem.full",
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode(
                            value = "food",
                            subgraph = "Food.basic"
                        ),
                        NamedAttributeNode(
                            value = "complement",
                            subgraph = "Complement.basic"
                        ),
                        NamedAttributeNode(
                            value = "addOns",
                            subgraph = "AddOn.basic"
                        ),
                        NamedAttributeNode(
                            value = "appliedDiscounts",
                            subgraph = "AppliedDiscount.withDiscount"
                        ),
                        NamedAttributeNode("quantity"),
                        NamedAttributeNode("totalAmount"),
                    ]
                ),

                // level 3 relationships
                // Complement info
                NamedSubgraph(
                  name = "Complement.basic",
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("complementName"),
                        NamedAttributeNode("price")
                    ]
                ),


                // Add on info
                NamedSubgraph(
                    name = "AddOn.basic",
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("addOnName"),
                        NamedAttributeNode("price")
                    ]
                ),

                // Level 2
                // Applied discount info
                NamedSubgraph(
                    name = "AppliedDiscount.withDiscount",
                    attributeNodes = [
                        NamedAttributeNode(
                            value = "discount",
                            subgraph = "Discount.basic"
                        )
                    ]
                ),

                // level 3
                // Discount info
                NamedSubgraph(
                    name = "Discount.basic",
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("discountName"),
                        NamedAttributeNode("percentage")
                    ]
                ),
            ]
        )
    ]
)*/
@Entity
@Table(name = "orders")
class Order {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "internal_order_id")
    var internalOrderId: String? = ""

    @Column(name = "user_order_code")
    var userOrderCode: String? = ""

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer? = null

    @ManyToOne
    @JoinColumn(name = "customer_address_id", nullable = true)
    var customerAddress: CustomerAddress? = null

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    var vendor: Vendor? = null

    @get:Transient
    val items: List<OrderEntity> get() = (foodOrderItems + beverageOrderItems).sortedBy { it.addedAt }

    @JsonBackReference
    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodOrderItems: MutableList<FoodOrderItem> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var beverageOrderItems: MutableList<BeverageOrderItem> = mutableListOf()

    @Column(name = "placement_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @CreationTimestamp
    var placementTime: LocalDateTime? = null

    @Column(name = "total_amount")
    var totalAmount: Long? = null

    @Column(name = "response_time", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var responseTime: LocalDateTime? = null

    @Column(name = "delivery_time", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var deliveryTime: LocalTime? = null

    @Column(name = "ready_by", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var readyBy: LocalTime? = null

    @Column(name = "delivery_fee", nullable = true)
    var deliveryFee: Long? = 0

    @Column(name = "order_type", nullable = false)
    var orderType: OrderType? = null

    @Column(name = "order_status", nullable = false)
    var orderStatus: OrderStatus? = null

    @Column(name = "completed_time", nullable = true)
    var completedTime: LocalDateTime? = null

    @Column(name = "customer_deleted_status", nullable = false)
    var customerDeletedStatus: Boolean? = false

    @Column(name = "vendor_deleted_status", nullable = false)
    var vendorDeletedStatus: Boolean? = false

    @Version
    var version: Long = 0

    fun addItem(item: OrderEntity) {
        when (item) {
            is FoodOrderItem -> { foodOrderItems.add(item.apply { order = this@Order }) }
            is BeverageOrderItem -> { beverageOrderItems.add(item.apply { order = this@Order }) }
        }
    }

    fun addAllItems(itemsList: List<OrderEntity>) {
        for (item in itemsList) {
            when(item) {
                is FoodOrderItem -> { foodOrderItems.add(item.apply { order = this@Order }) }
                is BeverageOrderItem -> {  beverageOrderItems.add(item.apply { order = this@Order }) }
            }
        }
    }

    /**
     * Calculates the subtotal, delivery fee, and total including delivery.
     * @returns [Triple]: (subtotalWithoutDelivery, deliveryFee, totalWithDelivery)
     */
    fun calculateTotals(): Triple<Long, Long, Long> {

        // Calculate subtotal (sum of food items + beverages)
        val subtotal = items.sumOf { it.totalAmount ?: 0 }

        // Calculate delivery fee
        val deliveryFee = if (items.isEmpty()) {
            0L
        } else {
            val fees = items.map {
                when(it) {
                    is FoodOrderItem -> it.food.deliveryFee ?: 0L
                    else -> 0
                }

            }

            if (fees.size == 1) fees.first()
            else {
                val maxFee = fees.maxOrNull() ?: 0L
                val sumOfOthers = fees.filter { it != maxFee }.sum()
                val averageDividedByTwo = (sumOfOthers.toDouble() / (fees.size - 1) / 2)
                (maxFee + averageDividedByTwo).roundToLong()
            }
        }

        // Assign deliveryFee to the order for persistence
        this.deliveryFee = if (this.orderType == OrderType.DELIVERY) {
            deliveryFee
        } else 0L

        // Calculate total including delivery
        val total = subtotal + deliveryFee

        return Triple(subtotal, deliveryFee, total)
    }
}

