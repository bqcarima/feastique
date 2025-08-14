package com.qinet.feastique.service

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.discount.FoodDiscount
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodAvailability
import com.qinet.feastique.model.entity.food.FoodImage
import com.qinet.feastique.model.entity.food.FoodOrderType
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.model.enums.Day
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.discount.DiscountRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.food.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.security.UserVerification
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.iterator

@Service
class FoodService(
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val complementRepository: ComplementRepository,
    private val addOnRepository: AddOnRepository,
    private val discountRepository: DiscountRepository,
    private val userVerification: UserVerification
) {

    @Transactional(readOnly = true)
    fun getFoodById(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity

    ): Food {
        userVerification.verifyVendor(vendorId, vendorDetails)
        val food = foodRepository.findById(id)
            .orElseThrow { IllegalArgumentException("No discount found for id: $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete discount: $id")
                }
            }
        return food
    }

    @Transactional(readOnly = true)
    fun getAllFoods(vendorId: Long, vendorDetails: UserSecurity): List<Food> {

        val vendor = userVerification.verifyVendor(vendorId, vendorDetails)
        val foods = foodRepository.findAllByVendorId(vendor.id!!)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("No food found for the vendor: $vendorId")

        require(foods.all { it ->
            it.vendor.id == vendorDetails.id
        }) {
            throw IllegalArgumentException("You (vendor ${vendorDetails.id}) does not have the permission to access these foods.")
        }
        return foods
    }

    @Transactional(readOnly = true)
    fun getDuplicate(foodName: String, vendorId: Long): Boolean =
        foodRepository.findByFoodNameIgnoreCaseAndVendorId(foodName, vendorId) != null

    @Transactional
    fun delete(
        id: Long,
        vendorId: Long,
        vendorDetails: UserSecurity
    ) {
        if (vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to delete foods for vendor $vendorId")
        }

        val food = getFoodById(id, vendorId, vendorDetails)
        if (food.vendor.id != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to update food ${food.id}")
        }
        foodRepository.delete(food)
    }

    @Transactional
    fun saveFood(food: Food): Food {
        return foodRepository.save(food)
    }

    @Transactional
    fun addOrUpdateFood(
        foodDto: FoodDto,
        vendorDetails: UserSecurity
    ): FoodResponse {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { IllegalArgumentException("Vendor not found.") }

        // Information meant for the food table
        // Determine if food is to be created or update an existing one
        var food: Food = if (foodDto.id != null) {
            foodRepository.findById(foodDto.id!!)
                .orElseThrow { IllegalArgumentException("Food not found.") }
                .also {
                    if (it.vendor.id != vendorDetails.id) {
                        throw IllegalArgumentException("You do not have permission to update food ${it.foodName}")
                    }
                }
        } else {
            Food().apply {
                this.vendor = vendor
            }
        }

        if (foodDto.id == null) {

            // Check if the vendor has already added food with the same name
            if (!getDuplicate(foodDto.foodName!!, vendorDetails.id)) {
                food.foodName = foodDto.foodName ?: throw IllegalArgumentException("Please enter a food name.")
            } else {
                throw IllegalArgumentException("A food with the name: ${foodDto.foodName} already exist. Cannot add duplicate.")
            }
        } else {
            food.foodName = foodDto.foodName ?: throw IllegalArgumentException("Please enter a food name.")
        }

        food.mainCourse = foodDto.mainCourse ?: throw IllegalArgumentException("Please enter a main course.")
        food.description = foodDto.description ?: throw IllegalArgumentException("Please enter a description.")
        food.basePrice = foodDto.basePrice ?: throw IllegalArgumentException("Please enter a base price.")

        food = saveFood(food)

        // Remove old relationships when updating
        if (foodDto.id != null) {
            food.foodComplement.clear()
            food.foodSize.clear()
            food.foodAddOn.clear()
            food.foodOrderType.clear()
            food.foodImage.clear()
            food.foodAvailability.clear()
        }

        // Information meant for the food_image table
        val foodImages = foodDto.foodImage.map { it ->
            FoodImage().apply {
                this.imageUrl = it
                this.food = food
            }
        }
        food.foodImage.addAll(foodImages)

        // Information meant for the food_complement table
        val complements = complementRepository.findAllByIdInAndVendorId(
            foodDto.complementIds,
            vendor.id!!
        )

        val foodComplements = complements.map { it ->
            FoodComplement().apply {
                this.food = food
                this.complement = it
            }
        }
        food.foodComplement.addAll(foodComplements)

        // Information meant for the discount table
        // Get user selection from the food dto
        val newDiscounts = foodDto.discountIds

        // Get the discounts from the database
        val discountsFromDb = if (!newDiscounts.isNullOrEmpty()) {
            discountRepository.findAllByIdInAndVendorId(newDiscounts, vendorDetails.id)
        } else emptyList()

        // Creating a mutable map of existing assigned discounts
        val existingDiscountsById = food.foodDiscount
            .filter { it.discount.id != null }
            .associateBy { it.discount.id }
            .toMutableMap()

        if (foodDto.id != null) {

            // Remove all discount mappings not present in the incoming dto
            val toRemove = food.foodDiscount.filter { fd ->
                val discountId = fd.discount.id
                discountId == null || newDiscounts?.contains(discountId) != true
            }

            if (toRemove.isNotEmpty()) {

                // Remove all discounts not included in the current collection.
                // JPA will also delete the rows because orphanRemoval = true.
                food.foodDiscount.removeAll(toRemove)

                // Removing discounts from the lookup map if present
                toRemove.forEach { fd ->
                    fd.discount.id?.let {
                        existingDiscountsById.remove(it)
                    }
                }
            }
        }

        // Add any new mapping for discounts that are not already assigned.
        for (discount in discountsFromDb) {
            val discountId = discount.id
            if (!existingDiscountsById.containsKey(discountId)) {
                val foodDiscount = FoodDiscount().apply {
                    this.food = food
                    this.discount = discount
                    this.active = true
                }
                food.foodDiscount.add(foodDiscount)
                existingDiscountsById[discountId] = foodDiscount
            }
        }

        // Explicitly assigning an active status
        val discountActiveMap: Map<Long, Boolean> = foodDto.discountActive ?: emptyMap()
        for ((discountId, activeFlag) in discountActiveMap) {
            val foodDiscount = existingDiscountsById[discountId]
            if (foodDiscount != null) {

                // if the discount is already assigned, set its active status to true
                foodDiscount.active = activeFlag

            } else {

                // Discount is not assigned, assign it.
                val retrievedDiscount = discountRepository.findById(discountId)
                    .orElseThrow { IllegalArgumentException("Discount not found") } // returns an Optional<Discount>

                if (retrievedDiscount.vendor.id == vendorDetails.id) {
                    val newFoodDiscount = FoodDiscount().apply {
                        this.food = food
                        this.discount = retrievedDiscount
                        this.active = activeFlag
                    }
                    food.foodDiscount.add(newFoodDiscount)
                    existingDiscountsById[discountId] = newFoodDiscount

                } else {
                    throw IllegalArgumentException("You do not have the permission to assign discount.")
                }
            }
        }

        // Information meant for the food_order_type table
        val foodSizes = foodDto.foodSize.map {
            FoodSize().apply {
                this.food = food
                this.size = when (it) {
                    "medium" -> Size.MEDIUM.type
                    "large" -> Size.LARGE.type
                    "extra-large" -> Size.EXTRA_LARGE.type
                    else -> {
                        throw IllegalArgumentException("$it is not a valid food size.")
                    }
                }
            }
        }
        food.foodSize.addAll(foodSizes)

        // Information meant for the food_order_type table
        val foodOrderTypes = foodDto.orderType.map { order ->
            FoodOrderType().apply {
                this.food = food
                this.orderType = when (order.lowercase()) {
                    "delivery" -> OrderType.DELIVERY.type
                    "dine-in" -> OrderType.DINE_IN.type
                    "takeaway" -> OrderType.TAKEAWAY.type
                    else -> throw IllegalArgumentException("$order is not a valid order type.")
                }
            }
        }
        food.foodOrderType.addAll(foodOrderTypes)

        // Information meant for the food_add_on table
        val addOns = if (!foodDto.addOnIds.isNullOrEmpty()) {
            addOnRepository.findAllByIdInAndVendorId(foodDto.addOnIds!!, vendorDetails.id)
        } else emptyList()

        if (!addOns.isEmpty()) {
            val foodAddOns =
                addOns.map {
                    FoodAddOn().apply {
                        this.food = food
                        this.addOn = it
                    }
                }
            food.foodAddOn.addAll(foodAddOns)
        }

        // Information meant for the food_availability table
        val foodAvailabilities = mutableListOf<FoodAvailability>()

        if (foodDto.availability.size > 6 || foodDto.availability[0].equals(Day.ALL.type, ignoreCase = true)) {
            foodAvailabilities.add(
                FoodAvailability().apply {
                    this.food = food
                    this.availability = Day.ALL.type
                }
            )
        } else {

            val userSelection = foodDto.availability.map { availability ->
                FoodAvailability().apply {
                    this.food = food
                    this.availability = when (availability.lowercase()) {
                        "monday" -> Day.MONDAY.type
                        "tuesday" -> Day.TUESDAY.type
                        "wednesday" -> Day.WEDNESDAY.type
                        "thursday" -> Day.THURSDAY.type
                        "friday" -> Day.FRIDAY.type
                        "saturday" -> Day.SATURDAY.type
                        "sunday" -> Day.SUNDAY.type
                        else -> throw IllegalArgumentException("$availability is not a valid option.")
                    }
                }
            }
            foodAvailabilities.addAll(userSelection)
        }

        food.foodAvailability.addAll(foodAvailabilities)

        // Updating food with the new references to food complements in the food complement table.
        food = saveFood(food)

        // Reload the saved food with all relationships
        val savedFood = foodRepository.findByIdWithAllRelations(food.id!!)
            .orElseThrow { EntityNotFoundException("Food not found.") }

        return savedFood.toResponse()

    }
}