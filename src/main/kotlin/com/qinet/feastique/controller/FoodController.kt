package com.qinet.feastique.controller

import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodOrderType
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.response.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.addOn.FoodAddOnService
import com.qinet.feastique.service.complement.FoodComplementService
import com.qinet.feastique.service.food.FoodOrderTypeService
import com.qinet.feastique.service.food.FoodService
import com.qinet.feastique.service.food.FoodSizeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/food")
class FoodController(
    private val foodService: FoodService,
    private val foodComplementService: FoodComplementService,
    private val foodSizeService: FoodSizeService,
    private val foodAddOnService: FoodAddOnService,
    private val foodOrderTypeService: FoodOrderTypeService
) {

    @PostMapping("/add")
    fun addFood(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid foodDto: FoodDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): FoodResponse {
        return foodService.addFood(foodDto, vendorDetails)
    }

    @DeleteMapping("/delete/{foodId}")
    fun deleteFood(
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<String> {

        val food = foodService.getFoodById(foodId)
            .orElseThrow { IllegalArgumentException("Food with id: $foodId found. Unable to delete food.") }
            .also {
                if(it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to update food ${it.foodName}")
                }
            }

        foodAddOnService.deleteAllFoodAddOnsByFoodId(foodId)
        foodComplementService.deleteAllFoodComplements(foodId)
        foodSizeService.deleteAllFoodSizes(foodId)
        foodOrderTypeService.deleteAllFoodOrderTypes(foodId)
        foodService.delete(food)

        return ResponseEntity("Food deleted successfully. All associations will be deleted as well.",HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAllFood(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : List<Food> {
        if(vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to view these food sizes.")
        }
        return foodService.getAllFoods(vendorDetails.id)
    }

    @DeleteMapping("/delete/{foodId}/food_size/{id}")
    fun deleteFoodSize(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) {
        if(vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to delete this food size.")
        }
        val foodSize = foodSizeService.getFoodSize(id, foodId) ?: Exception("Food size not found for food id: $foodId")
        foodSizeService.deleteFoodSize(foodSize as FoodSize)

    }

    @PostMapping("/delete/{foodId}/order_type/{id}")
    fun deleteFoodOrderType(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) {
        if(vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to delete this food order type.")
        }
        val foodOrderType = foodOrderTypeService.getOrderType(id, foodId) ?: Exception("Food order type not found for food id: $foodId")
        foodOrderTypeService.deleteFoodOrderType(foodOrderType as FoodOrderType)
    }
}

