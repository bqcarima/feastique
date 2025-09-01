package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.order.FoodOrder
import com.qinet.feastique.model.enums.Size
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "food_size")
class FoodSize {

    @Id
    @GeneratedValue
    var id: Long? = null

    @NotNull(message = "Please select at least one size.")

    var size: Size? = null

    @Column(name = "price_increase")
    // @NotNull(message = "Please enter price increase.")
    var priceIncrease: Long? = 0

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

    // Food order relationship
    @JsonBackReference // prevent infinite recursion for extra protection
    @OneToMany(
        mappedBy = "size",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    @OrderColumn(name = "order_index")
    var foodSize: MutableList<FoodOrder> = mutableListOf()
}

