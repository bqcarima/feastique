package com.qinet.feastique.model.entity.image

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.consumables.food.Food
import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

@MappedSuperclass
abstract class Image {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "image_url", nullable = false)
    @NotBlank(message = "Image cannot be null.")
    @NotEmpty(message = "Image cannot be empty.")
    var imageUrl: String? = ""
}

@Entity
@Table(name = "beverage_images")
class BeverageImage : Image() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage
}


@Entity
@Table(name = "dessert_images")
class DessertImage : Image() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert
}


@Entity
@Table(name = "food_images")
class FoodImage : Image() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id", nullable = false)
    @JsonIgnore
    lateinit var food: Food
}

@Entity
@Table(name = "handheld_images")
class HandheldImage : Image() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "handheld_id", nullable = false)
    @JsonIgnore
    lateinit var handheld: Handheld
}

