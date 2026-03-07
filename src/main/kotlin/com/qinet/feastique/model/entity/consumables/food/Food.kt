package com.qinet.feastique.model.entity.consumables.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.consumables.EdibleEntity
import com.qinet.feastique.model.entity.consumables.addOn.FoodAddOn
import com.qinet.feastique.model.entity.consumables.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.image.FoodImage
import com.qinet.feastique.model.entity.size.FoodSize
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.OrderType
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

@Suppress("JpaEntityGraphsInspection")
@Entity
@Table(name = "foods")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Food.withAllRelations",
            attributeNodes = [
                NamedAttributeNode("foodImages"),
                NamedAttributeNode(value = "foodAddOns", subgraph = "addOn-subgraph"),
                NamedAttributeNode("foodSizes"),
                NamedAttributeNode(value = "foodComplements", subgraph = "foodComplement-subgraph"),
                NamedAttributeNode(value = "foodDiscounts", subgraph = "discount-subgraph"),
                NamedAttributeNode("orderTypes"),
                NamedAttributeNode("availableDays")
            ],
            subgraphs = [
                NamedSubgraph(
                    name = "addOn-subgraph",
                    attributeNodes = [NamedAttributeNode("addOn")]
                ),
                NamedSubgraph(
                    name = "foodComplement-subgraph",
                    attributeNodes = [NamedAttributeNode("complement")]
                ),
                NamedSubgraph(
                    name = "discount-subgraph",
                    attributeNodes = [NamedAttributeNode("discount")]
                )
            ]
        )
    ]
)
class Food : EdibleEntity() {

    @Column(name = "food_number", unique = true)
    var foodNumber: String? = null

    @Column(name = "ready_as_from", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var readyAsFrom: LocalTime? = null

    @ElementCollection(targetClass = Day::class)
    @CollectionTable(
        name = "food_available_days",
        joinColumns = [JoinColumn(name = "food_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day")
    var availableDays: MutableSet<Day> = mutableSetOf()

    @ElementCollection(targetClass = OrderType::class)
    @CollectionTable(
        name = "food_order_types",
        joinColumns = [JoinColumn(name = "food_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "order_types")
    var orderTypes: MutableSet<OrderType> = mutableSetOf()

    @Column(name = "main_course")
    @NotEmpty(message = "Main course cannot be empty.")
    @NotNull(message = "Main course cannot be empty.")
    var mainCourse: String? = null

    @NotEmpty(message = "Description cannot be empty.")
    @NotNull(message = "Description cannot be empty.")
    var description: String? = null

    @Column(name = "base_price")
    var basePrice: Long? = 0

    @Column(name = "delivery_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var deliveryTime: LocalTime? = null

    @Column(name = "delivery_fee", nullable = false)
    var deliveryFee: Long? = 0

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodImages: MutableSet<FoodImage> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAddOns: MutableSet<FoodAddOn> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodComplements: MutableSet<FoodComplement> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodDiscounts: MutableSet<FoodDiscount> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodSizes: MutableSet<FoodSize> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    lateinit var menu: Menu
}

