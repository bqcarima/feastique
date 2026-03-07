package com.qinet.feastique.model.dto

import com.qinet.feastique.model.entity.consumables.addOn.AddOn
import com.qinet.feastique.model.entity.consumables.complement.Complement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Used to change beverage availability, flavour availability, and flavour sizes.
 * @param beverageId
 * @param availability
 * @param beverageFlavours
 */
data class BeverageAvailabilityDto(
    @field:NotNull(message = "Beverage Id cannot be empty.")
    var beverageId: UUID,

    var availability: String?,
    var beverageFlavours: Set<@Valid FlavourAvailabilityDto>?
)


/**
 * Used to change dessert flavour availability.
 * @param dessertId
 * @param dessertFlavours
 */
data class DessertAvailabilityDto(
    @field:NotNull(message = "Dessert Id cannot be empty.")
    var dessertId: UUID,
    var availability: String?,
    var dessertFlavours: Set<@Valid FlavourAvailabilityDto>?
)


/**
 * Used to change food availability, add-on and complement availability, and size availability.
 * @param foodId
 * @param availability
 * @param addOns
 * @param complements
 * @param foodSizes
 */
data class FoodAvailabilityDto(
    @field:NotNull(message = "Food Id cannot be empty.")
    var foodId: UUID,

    var availability: String?,
    var addOns: Set<@Valid ItemAvailabilityDto>?,
    var complements: Set<@Valid ItemAvailabilityDto>?,
    var foodSizes: Set<@Valid SizeAvailabilityDto>?
)

/**
 * Used to change handheld availability and sizes.
 * @param id
 * @param availability
 * @param handheldSizes
 */
data class HandheldAvailabilityDto(
    @field:NotNull(message = "Handheld ID cannot be empty.")
    var id: UUID?,

    var availability: String?,
    var handheldSizes: Set<@Valid SizeAvailabilityDto>?
)

/**
* Used to change [AddOn] and [Complement] availability.
 * @param itemId
 * @param availability
*/
data class ItemAvailabilityDto(
    var itemId: UUID?,
    var availability: String?
)

/**
 * Used to change flavour availability for beverages and desserts.
 * @param flavourId
 * @param availability
 * @param flavourSizes
 */
data class FlavourAvailabilityDto(
    @field:NotNull(message = "Flavour Id cannot be empty.")
    var flavourId: UUID?,

    var availability: String?,
    var flavourSizes: Set<@Valid SizeAvailabilityDto>?
)


/**
 * Used to change size availability for beverages, foods, and handhelds.
 * @param sizeId
 * @param availability
 */
data class SizeAvailabilityDto(
    @field:NotNull(message = "Size Id cannot be empty.")
    var sizeId: UUID?,

    var availability: String?
)

