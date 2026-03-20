package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.BeverageAvailabilityDto
import com.qinet.feastique.model.dto.consumables.BeverageDto
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.BeverageService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class BeverageController(private val beverageService: BeverageService, private val securityUtility: SecurityUtility) {

    @PutMapping("/vendors/{vendorId}/beverages")
    fun addOrUpdateBeverage(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid beverageDto: BeverageDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<BeverageResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beverage = beverageService.addOrUpdateBeverage(beverageDto, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/vendors/{vendorId}/beverages/{id}")
    fun deleteBeverage(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        beverageService.deleteBeverage(id, vendorDetails)
        return ResponseEntity("Beverage deleted successfully.", HttpStatus.NO_CONTENT)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/beverages/{beverageId}",
            "/vendors/{vendorId}/beverages/{beverageId}"
        ]
    )
    fun getFood(
        @PathVariable beverageId: UUID,
        @PathVariable customerId: UUID?,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<BeverageResponse> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val beverage = beverageService.getBeverage(beverageId, userDetails)
        return ResponseEntity(beverage, HttpStatus.OK)
    }

    @GetMapping("/vendors/{vendorId}/beverages")
    fun getAllBeverages(
        @PathVariable vendorId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<BeverageResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beveragePage = beverageService.getAllBeverages(vendorDetails, page, size)
        return ResponseEntity(beveragePage.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/beverages/scroll",
            "/vendors/{vendorId}/beverages/scroll"
        ]
    )
    fun scrollBeverages(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BeverageResponse>> {
        require(size in 1..50) { "Page size must be between 1 and 20." }

        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val window = beverageService.scrollBeverages(vendorId, cursor, size, userDetails)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @PatchMapping("/vendors/{vendorId}/beverages/availability/{id}")
    fun changeBeverageAvailability(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @RequestBody @Valid beverageAvailabilityDto: BeverageAvailabilityDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<BeverageResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beverage = beverageService.changeBeverageAvailability(beverageAvailabilityDto, id, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.OK)
    }
}

