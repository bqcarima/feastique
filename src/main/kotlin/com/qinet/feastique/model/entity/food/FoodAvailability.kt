package com.qinet.feastique.model.entity.food

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "food_availability")
class FoodAvailability {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @NotBlank(message = "Availability cannot be null.")
    @NotEmpty(message = "Availability cannot be empty.")
    var availability: String? = ""

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

