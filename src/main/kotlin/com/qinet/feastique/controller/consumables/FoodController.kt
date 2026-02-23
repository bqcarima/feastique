package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.consumables.FoodDto
import com.qinet.feastique.response.PageResponse
import com.qinet.feastique.response.consumables.food.FoodResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.FoodService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/foods")
class FoodController(
    private val foodService: FoodService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateFood(
        @PathVariable vendorId: UUID,
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
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        foodService.deleteFood(id, vendorDetails)
        return ResponseEntity("Food deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getFood(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<FoodResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val food = foodService.getFoodById(id, vendorDetails)
        return ResponseEntity(food.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllFood(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<FoodResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val foodsPage= foodService.getAllFoods(vendorDetails, page, size)
        return ResponseEntity(foodsPage.toResponse(), HttpStatus.OK)
    }
}

