package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.FoodAvailabilityDto
import com.qinet.feastique.model.dto.consumables.FoodDto
import com.qinet.feastique.response.consumables.food.FoodResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.FoodService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class FoodController(
    private val foodService: FoodService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping("/vendors/{vendorId}/foods")
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

    @DeleteMapping("/vendors/{vendorId}/foods/{id}")
    fun deleteFood(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        foodService.deleteFood(id, vendorDetails)
        return ResponseEntity("Food deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/vendors/{vendorId}/foods/{id}")
    fun getFood(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<FoodResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val food = foodService.getFoodById(id, vendorDetails)
        return ResponseEntity(food.toResponse(), HttpStatus.OK)
    }

    @GetMapping("/vendors/{vendorId}/foods")
    fun getAllFood(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<FoodResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val foodsPage = foodService.getAllFoods(vendorDetails, page, size)
        return ResponseEntity(foodsPage.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendor/{vendorId}/foods/scroll",
            "/vendors/{vendorId}/foods/scroll"
        ]
    )
    fun scrollFoods(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<WindowResponse<FoodResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val window = foodService.scrollFoods(vendorId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @PatchMapping("/vendors/{vendorId}/foods/availability/{id}")
    fun changeFoodAvailability(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @RequestBody @Valid foodAvailabilityDto: FoodAvailabilityDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<FoodResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val food = foodService.changeFoodAvailability(foodAvailabilityDto, id, vendorDetails)
        return ResponseEntity(food.toResponse(), HttpStatus.OK)
    }
}

