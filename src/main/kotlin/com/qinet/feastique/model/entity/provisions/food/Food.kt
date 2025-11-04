package com.qinet.feastique.model.entity.provisions.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.provisions.addOn.FoodAddOn
import com.qinet.feastique.model.entity.provisions.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.provisions.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
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
                NamedAttributeNode("foodImage"),
                NamedAttributeNode(value = "foodAddOn", subgraph = "addOn-subgraph"),
                NamedAttributeNode("foodSize"),
                NamedAttributeNode(value = "foodComplement", subgraph = "foodComplement-subgraph"),
                NamedAttributeNode(value = "foodDiscount", subgraph = "discount-subgraph"),
                NamedAttributeNode("foodOrderType"),
                NamedAttributeNode("foodAvailability")
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
class Food : BaseEntity() {
    @Column(name = "food_number", unique = true)
    var foodNumber: String? = null

    @Column(name = "main_course")
    @NotBlank(message = "Main course cannot be null.")
    @NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String? = ""

    @NotBlank(message = "Description cannot be null.")
    @NotEmpty(message = "Description cannot be empty.")
    var description: String? = ""

    @Column(name = "preparation_time")
    @NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int? = 0

    @Column(name = "delivery_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var deliveryTime: LocalTime? = null

    @Column(name = "delivery_fee")
    var deliveryFee: Long? = 0

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodImage: MutableSet<FoodImage> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAddOn: MutableSet<FoodAddOn> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAvailability: MutableSet<FoodAvailability> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodComplement: MutableSet<FoodComplement> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodDiscount: MutableSet<FoodDiscount> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodOrderType: MutableSet<FoodOrderType> = mutableSetOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodSize: MutableSet<FoodSize> = mutableSetOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    lateinit var menu: Menu
}

