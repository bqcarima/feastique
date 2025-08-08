package com.qinet.feastique.service.food

import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.model.enums.Size
import com.qinet.feastique.repository.complement.ComplementRepository
import com.qinet.feastique.repository.complement.FoodComplementRepository
import com.qinet.feastique.repository.food.FoodRepository
import com.qinet.feastique.repository.food.FoodSizeRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.FoodResponse
import com.qinet.feastique.response.FoodSizeResponse
import com.qinet.feastique.security.UserSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.Locale
import java.util.Optional

@Service
class FoodService(
    private val foodRepository: FoodRepository,
    private val vendorRepository: VendorRepository,
    private val complementRepository: ComplementRepository,
    private val foodComplementRepository: FoodComplementRepository,
    private val foodSizeRepository: FoodSizeRepository
) {

    @Transactional(readOnly = true)
    fun getFoodById(foodId: Long): Optional<Food> {
        return foodRepository.findById(foodId)
    }

    @Transactional(readOnly = true)
    fun getAllFoods(vendorId: Long): List<Food> {
        return foodRepository.findAllByVendorId(vendorId)
    }

    @Transactional
    fun delete(food: Food) {
        foodRepository.delete(food)
    }

    @Transactional
    fun deleteByFoodIdAndVendorId(foodId: Long, vendorId: Long) {
        foodRepository.deleteByIdAndVendorId(foodId, vendorId)
    }

    @Transactional
    fun saveFood(food: Food): Food {
        return foodRepository.save(food)
    }

    @Transactional
    fun addFood(
        foodDto: FoodDto,
        @AuthenticationPrincipal
        vendorDetails: UserSecurity
    ): FoodResponse {
        val vendor = vendorRepository.findById(vendorDetails.id).get()

        // Information meant for the food table
        var food = Food()
        food.foodName = foodDto.foodName ?: throw IllegalArgumentException("Please enter a food name.")
        food.mainCourse = foodDto.mainCourse ?: throw IllegalArgumentException("Please enter a main course.")
        food.description = foodDto.description ?: throw IllegalArgumentException("Please enter a description.")
        food.basePrice = foodDto.basePrice ?: throw IllegalArgumentException("Please enter a base price.")
        food.image = foodDto.image
        food.vendor = vendor

        food = saveFood(food)

        // Information meant for the food_complement table
        val complementSelection: List<Long> = foodDto.complementIds ?: throw Exception("Error parsing list of complementIds")
        val complements = complementRepository.findAllByIdInAndVendorId(complementSelection, vendor.id!!)
        val foodComplements = mutableListOf<FoodComplement>()

        for(complement in complements) {
            val foodComplement = FoodComplement()
            foodComplement.food = food
            foodComplement.complement = complement
            foodComplements.add(foodComplement)
        }
        foodComplementRepository.saveAll(foodComplements)

        // Information meant for the food_order_type table
        val foodSizeSelection: List<String> = foodDto.foodSize ?: throw Exception("Error parsing list of food order type")
        val foodSizes = mutableListOf<FoodSize>()
        for(size in foodSizeSelection) {
            val foodSize = FoodSize()
            when (size.lowercase(Locale.ENGLISH)) {
                "medium" -> {
                    foodSize.food = food
                    foodSize.size = Size.MEDIUM.name

                }
                "large" -> {
                    foodSize.food = food
                    foodSize.size = Size.LARGE.name
                }
                "extra-large" -> {
                    foodSize.food = food
                    foodSize.size = Size.EXTRA_LARGE.name
                }
                else -> {
                    throw IllegalArgumentException("$size is not a valid food size.")
                }
            }
            foodSizes.add(foodSize)
        }
        foodSizeRepository.saveAll(foodSizes)


        /**
         * Updating food with the new references to
         * food complements in the food complement table.
         */

        food = saveFood(food)

        val complementResponse = complements.map { it ->
            ComplementResponse(
                id = it.id ?: 0,
                name = it.complementName ?: "",
                price = it.price ?: ""
            )
        }

        val foodSizeResponse = foodSizes.map { it ->
            FoodSizeResponse(
                id = it.id ?: 0,
                size = it.size ?: ""
            )
        }

        return FoodResponse(
            id = food.id!!,
            foodName = food.foodName!!,
            vendorId = food.vendor.id!!,
            vendorName = food.vendor.chefName!!,
            mainCourse = food.mainCourse!!,
            description = food.description!!,
            basePrice = food.basePrice!!,
            size = foodSizeResponse,
            complements = complementResponse,
            addOn = emptyList(),
            orderType = emptyList(),
            image = emptyList()
        )

    }
}

