package com.qinet.feastique.service.food

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodImage
import com.qinet.feastique.model.entity.food.FoodOrderType
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.addOn.AddOnRepository
import com.qinet.feastique.repository.addOn.FoodAddOnRepository
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.complement.FoodComplementRepository
import com.qinet.feastique.repository.food.FoodImageRepository
import com.qinet.feastique.repository.food.FoodOrderTypeRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.food.FoodSizeRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.FoodResponse
import com.qinet.feastique.security.UserSecurity
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FoodService(
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val complementRepository: ComplementRepository,
    private val foodComplementRepository: FoodComplementRepository,
    private val foodSizeRepository: FoodSizeRepository,
    private val addOnRepository: AddOnRepository,
    private val foodAddOnRepository: FoodAddOnRepository,
    private val foodOrderTypeRepository: FoodOrderTypeRepository,
    private val foodImageRepository: FoodImageRepository
) {

    @Transactional(readOnly = true)
    fun getFoodById(foodId: Long): Optional<Food> {
        return foodRepository.findById(foodId)
    }

    @Transactional(readOnly = true)
    fun getAllFoods(vendorId: Long): List<Food> {
        return foodRepository.findAllByVendorId(vendorId)
    }

    @Transactional(readOnly = true)
    fun getDuplicate(foodName: String, vendorId: Long): Food? {
        return foodRepository.findByFoodNameIgnoreCaseAndVendorId(foodName, vendorId)
    }

    @Transactional
    fun delete(food: Food) {
        foodRepository.delete(food)
    }

    @Transactional
    fun saveFood(food: Food): Food {
        return foodRepository.save(food)
    }

    @Transactional
    fun addFood(
        foodDto: FoodDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): FoodResponse {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { IllegalArgumentException("Vendor not found.") }

        // Information meant for the food table
        // Determine if food is to be created or update an existing one
        var food: Food = if(foodDto.id != null) {
            foodRepository.findById(foodDto.id!!)
                .orElseThrow { IllegalArgumentException("Food not found.") }
                .also {
                    if(it.vendor.id != vendorDetails.id) {
                        throw IllegalArgumentException("You do not have permission to update food ${it.foodName}")
                    }
                }
        } else {
            Food().apply {
                this.vendor = vendor
            }
        }

        if(foodDto.id == null) {

            // Check if the vendor has already added food with the same name
            if(getDuplicate(foodDto.foodName!!, vendorDetails.id) == null) {
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
        if(foodDto.id != null) {
            food.foodComplement.clear()
            food.foodSize.clear()
            food.foodAddOn.clear()
            food.foodOrderType.clear()
            food.foodImage.clear()
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

        // Information meant for the food_order_type table
        val foodSizes = foodDto.foodSize.map {it
            FoodSize().apply {
                this.food = food
                this.size = when(it) {
                    "medium" -> Size.MEDIUM.type
                    "large" -> Size.LARGE.type
                    "extra-large" -> Size.EXTRA_LARGE.type
                    else -> { throw IllegalArgumentException("$it is not a valid food size.")
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
        val addOns = if(!foodDto.addOnIds.isNullOrEmpty()) {
            addOnRepository.findAllByIdInAndVendorId(foodDto.addOnIds!!, vendorDetails.id)
        } else emptyList()

        if(!addOns.isEmpty()) {
            val foodAddOns =
                addOns.map {
                    FoodAddOn().apply {
                        this.food = food
                        this.addOn = it
                    }
                }
            food.foodAddOn.addAll(foodAddOns)
        }

        // Updating food with the new references to food complements in the food complement table.
        food = saveFood(food)

        // Reload the saved food with all relationships
        val savedFood = foodRepository.findByIdWithAllRelations(food.id!!)
            .orElseThrow { EntityNotFoundException("Food not found.") }

        return savedFood.toResponse()

    }
}

