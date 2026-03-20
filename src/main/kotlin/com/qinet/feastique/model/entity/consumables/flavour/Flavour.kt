package com.qinet.feastique.model.entity.consumables.flavour

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.entity.size.BeverageFlavourSize
import com.qinet.feastique.model.entity.size.DessertFlavourSize
import com.qinet.feastique.model.enums.Availability
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.*

@MappedSuperclass
abstract class Flavour {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @NotBlank(message = "Name cannot be null.")
    @NotEmpty(message = "Name cannot be empty.")
    var name: String? = ""

    var description: String? = ""

    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    var availability: Availability? = null

    @Column
    var isActive: Boolean = true
}

@Entity
@Table(name = "beverage_flavours")
class BeverageFlavour : Flavour() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    @JsonIgnore
    lateinit var beverage: Beverage

    @JsonBackReference
    @OneToMany(
        mappedBy = "beverageFlavour",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var beverageFlavourSizes: MutableSet<BeverageFlavourSize> = mutableSetOf()
}


@Entity
@Table(name = "dessert_flavours")
class DessertFlavour : Flavour() {

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id", nullable = false)
    @JsonIgnore
    lateinit var dessert: Dessert

    @JsonBackReference
    @OneToMany(
        mappedBy = "dessertFlavour",
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var dessertFlavourSizes: MutableSet<DessertFlavourSize> = mutableSetOf()
}

