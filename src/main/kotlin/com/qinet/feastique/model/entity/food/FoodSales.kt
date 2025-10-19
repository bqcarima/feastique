package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID


@Entity
@Table(name = "food_sales")
class FoodSales {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    var amount: Long? = null

    @Column(name = "sale_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @CreationTimestamp
    var saleDate: LocalDateTime? = null

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    lateinit var food: Food

    @ManyToOne
    @JoinColumn(name = "food_order_id", nullable = false)
    lateinit var foodOrderItem: FoodOrderItem

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    lateinit var vendor: Vendor
}

