package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.BeverageDto
import com.qinet.feastique.response.BeverageResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.BeverageService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/vendors/{vendorId}/beverages")
class BeverageController(private val beverageService: BeverageService, private val securityUtility: SecurityUtility) {

    @PutMapping
    fun addOrUpdateBeverage(
        @PathVariable("vendorId") vendorId: UUID,
        @RequestBody @Valid beverageDto: BeverageDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<BeverageResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beverage =  beverageService.addOrUpdateBeverage(beverageDto, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteBeverage(
        @PathVariable("id") id: UUID,
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        beverageService.deleteBeverage(id, vendorDetails)
        return ResponseEntity("Beverage deleted successfully.", HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun getBeverage(
        @PathVariable("id") id: UUID,
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<BeverageResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beverage = beverageService.getBeverage(id, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllBeverages(
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<List<BeverageResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val beverages = beverageService.getAllBeverages(vendorDetails)
        return ResponseEntity(beverages.map { it.toResponse() }, HttpStatus.OK)
    }
}