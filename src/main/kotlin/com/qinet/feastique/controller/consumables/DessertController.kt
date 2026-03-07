package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DessertAvailabilityDto
import com.qinet.feastique.model.dto.consumables.DessertDto
import com.qinet.feastique.response.PageResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
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
@RequestMapping("/api/v1/vendors/{vendorId}/desserts")
class DessertController(
    private val dessertService: DessertService,
    private val securityUtility: SecurityUtility
) {

    @GetMapping
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
    @PutMapping
    fun addOrUpdateDessert(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid dessertDto: DessertDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<DessertResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val dessert = dessertService.addOrUpdateDessert(dessertDto, vendorDetails)
        return ResponseEntity(dessert.toResponse(), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteDessert(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        dessertService.deleteDessert(id, vendorDetails)
        return ResponseEntity("Dessert deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @PatchMapping("/availability/{id}")
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

