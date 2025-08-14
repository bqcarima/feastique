package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.response.food.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.FoodService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/food")
class FoodController(
    private val foodService: FoodService
) {

    @PostMapping("/add")
    fun addOrUpdateFood(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid foodDto: FoodDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): FoodResponse {
        return foodService.addOrUpdateFood(foodDto, vendorDetails)
    }

    @DeleteMapping("/delete/{foodId}")
    fun deleteFood(
        @PathVariable vendorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        foodService.delete(vendorId, foodId, vendorDetails)
        return ResponseEntity("Food deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAllFood(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): List<FoodResponse> {
        return foodService.getAllFoods(vendorId, vendorDetails).map { it.toResponse() }
    }
}

