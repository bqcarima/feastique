package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "food_order_types")
class FoodOrderType {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "order_type", nullable = false)
    @NotBlank(message = "FoodOrderType name cannot be null.")
    @NotEmpty(message = "FoodOrderType name cannot be empty.")
    var orderType: String? = ""

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

