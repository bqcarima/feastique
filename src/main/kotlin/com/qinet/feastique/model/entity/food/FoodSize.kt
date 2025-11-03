package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.enums.Size
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "food_sizes")
class FoodSize {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @NotNull(message = "Please select at least one size.")
    var size: Size? = null

    @Column(name = "price_increase")
    // @NotNull(message = "Please enter price increase.")
    var priceIncrease: Long? = 0

    @NotBlank
    @NotEmpty(message = "Food name cannot be empty.")
    var name: String? = ""

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
    var foodSize: MutableList<FoodOrderItem> = mutableListOf()
}

