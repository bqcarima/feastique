package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.FoodDto
import com.qinet.feastique.response.food.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.FoodService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendors/{vendorId}/foods")
class FoodController(
    private val foodService: FoodService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateFood(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid foodDto: FoodDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<FoodResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val food = foodService.addOrUpdateFood(foodDto, vendorDetails)
        return ResponseEntity(food.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteFood(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        foodService.deleteFood(id, vendorDetails)
        return ResponseEntity("Food deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getFood(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<FoodResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val food = foodService.getFoodById(id, vendorDetails)
        return ResponseEntity(food.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllFood(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<List<FoodResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val foods= foodService.getAllFoods(vendorDetails)
        return ResponseEntity(foods.map { it.toResponse() }, HttpStatus.OK)
    }
}

