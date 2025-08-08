package com.qinet.feastique.model.entity.discount

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.food.Food
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "food_discount")
class FoodDiscount {

    @Id
    @GeneratedValue
    var id: Long? = null

    @NotBlank(message = "Active status cannot be null.")
    @NotEmpty(message = "Active status cannot be empty.")
    var active: String? = ""

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    lateinit var discount: Discount

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food

}