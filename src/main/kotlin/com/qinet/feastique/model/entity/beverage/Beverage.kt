package com.qinet.feastique.model.entity.beverage

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.BeverageGroup
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*

@Entity
@Table(name = "beverages")
class Beverage {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "name")
    @NotBlank(message = "Beverage name cannot be null.")
    @NotEmpty(message = "Beverage name cannot be empty.")
    var beverageName: String? = ""

    @NotNull(message = "Type status cannot be null.")
    var alcoholic: Boolean? = null

    var percentage: Int? = 0

    @Column(name = "beverage_group")
    @NotNull(message = "Beverage group cannot be empty.")
    @Enumerated(EnumType.STRING)
    var beverageGroup: BeverageGroup? = null

    @NotNull(message = "Price cannot be null.")
    var price: Long? = 0

    @NotNull(message = "Delivery availability cannot be null.")
    var delivery: Boolean? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}

