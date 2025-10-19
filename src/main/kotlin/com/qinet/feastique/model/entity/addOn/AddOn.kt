package com.qinet.feastique.model.entity.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.order.food.FoodOrderItem
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "add_ons")
class AddOn {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "add_on_name")
    @NotBlank(message = "Name cannot be null.")
    @NotEmpty(message = "Name cannot be empty.")
    var addOnName: String? = ""

    @NotNull(message = "Price cannot be null.")
    var price: Long? = 0

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    @JsonBackReference
    @OneToMany(
        mappedBy = "addOn",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()
}

