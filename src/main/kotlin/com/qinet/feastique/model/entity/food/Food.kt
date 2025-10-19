package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.Menu
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalTime
import java.util.UUID

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
class Food {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "food_number", unique = true)
    var foodNumber: String? = null

    @Column(name = "food_name")
    @NotBlank(message = "Food name cannot be null.")
    @NotEmpty(message = "Food name cannot be empty.")
    var foodName: String? = ""

    @Column(name = "main_course")
    @NotBlank(message = "Main course cannot be null.")
    @NotEmpty(message = "Main course cannot be empty.")
    var mainCourse: String? = ""

    @NotBlank(message = "Description cannot be null.")
    @NotEmpty(message = "Description cannot be empty.")
    var description: String? = ""

    @Column(name = "base_price")
    var basePrice: Long? = 0

    @Column(name = "preparation_time")
    @NotNull(message = "Preparation time cannot be empty.")
    var preparationTime: Int? = 0

    @Column(name = "delivery_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    var deliveryTime: LocalTime? = null

    @Column(name = "delivery_fee")
    var deliveryFee: Long? = 0

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodImage: MutableList<FoodImage> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodAvailability: MutableList<FoodAvailability> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodComplement: MutableList<FoodComplement> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodDiscount: MutableList<FoodDiscount> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodOrderType: MutableList<FoodOrderType> = mutableListOf()

    @JsonBackReference
    @OneToMany(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderColumn(name = "order_index")
    var foodSize: MutableList<FoodSize> = mutableListOf()

    @JsonManagedReference
    @OneToOne(
        mappedBy = "food",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    lateinit var menu: Menu
}

