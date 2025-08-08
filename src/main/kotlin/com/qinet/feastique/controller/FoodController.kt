package com.qinet.feastique.controller

import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.model.entity.food.Food
import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.response.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.food.FoodService
import com.qinet.feastique.service.complement.FoodComplementService
import com.qinet.feastique.service.food.FoodSizeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull
import java.lang.Exception

@RestController
@RequestMapping("/api/vendor/{vendorId}/food")
class FoodController(
    private val foodService: FoodService,
    private val foodComplementService: FoodComplementService,
    private val foodSizeService: FoodSizeService
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

    @PostMapping("/delete/{foodId}")
    fun deleteFood(
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<String> {

        if(vendorId != vendorDetails.id) {
            throw Exception("You do not have permission to delete this food.")
        }

        if(foodId.let { foodService.getFoodById(it).getOrNull()} != null) {
            foodComplementService.deleteAllFoodComplements(foodId)
            foodSizeService.deleteAllFoodSizes(foodId)
            foodService.deleteByFoodIdAndVendorId(foodId, vendorId)

            return ResponseEntity("Food deleted successfully. Complement associations will be deleted too.",HttpStatus.OK)
        } else {
            throw Exception("You are trying to delete a food that does not exist.")
        }
    }

    @DeleteMapping("/delete/{foodId}/food_size/{id}")
    fun deleteFoodSize(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long
    ) {
        val foodSize = foodSizeService.getFoodSize(id, foodId) ?: Exception("Food size not found for food id: $id")
        foodSizeService.deleteFoodSize(foodSize as FoodSize)

    }

    @GetMapping("/all")
    fun getAllFood(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : List<Food> {
        return foodService.getAllFoods(vendorDetails.id)
    }
}

