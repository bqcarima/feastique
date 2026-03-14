package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DessertAvailabilityDto
import com.qinet.feastique.model.dto.consumables.DessertDto
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.DessertService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class DessertController(
    private val dessertService: DessertService,
    private val securityUtility: SecurityUtility
) {

    @GetMapping("/vendors/{vendorId}/desserts")
    fun getAllDesserts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PageResponse<DessertResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val dessertsPage = dessertService.getAllDesserts(vendorDetails, page, size)
        return ResponseEntity(dessertsPage.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/desserts/scroll",
            "/vendors/{vendorId}/desserts/scroll"
        ]
    )
    fun getScrollDesserts(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<DessertResponse>> {
        require(size in 1..50) { "Page size must be between 1 and 20." }

        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)

        val window = dessertService.scrollDesserts(vendorId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @PutMapping("/vendors/{vendorId}/desserts")
    fun addOrUpdateDessert(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid dessertDto: DessertDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<DessertResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val dessert = dessertService.addOrUpdateDessert(dessertDto, vendorDetails)
        return ResponseEntity(dessert.toResponse(), HttpStatus.OK)
    }

    @DeleteMapping("/vendors/{vendorId}/desserts/{id}")
    fun deleteDessert(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        dessertService.deleteDessert(id, vendorDetails)
        return ResponseEntity("Dessert deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @PatchMapping("/vendors/{vendorId}/desserts/availability/{id}")
    fun changeDessertAvailability(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @RequestBody @Valid dessertAvailabilityDto: DessertAvailabilityDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<DessertResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val dessert = dessertService.changeDessertAvailability(dessertAvailabilityDto, id, vendorDetails)
        return ResponseEntity(dessert.toResponse(), HttpStatus.OK)
    }
}

