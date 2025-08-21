package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.BeverageDto
import com.qinet.feastique.response.BeverageResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.BeverageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/beverage")
class BeverageController(private val beverageService: BeverageService) {

    @PostMapping("/add")
    fun addOrUpdateBeverage(
        @PathVariable("vendorId") vendorId: Long,
        @RequestBody @Valid beverageDto: BeverageDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<BeverageResponse> {
        val beverage =  beverageService.addOrUpdateBeverage(beverageDto, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteBeverage(
        @PathVariable("id") id: Long,
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        beverageService.deleteBeverage(id, vendorDetails)
        return ResponseEntity("Beverage deleted successfully.", HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun getBeverage(
        @PathVariable("id") id: Long,
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<BeverageResponse> {
        val beverage = beverageService.getBeverage(id, vendorDetails)
        return ResponseEntity(beverage.toResponse(), HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAllBeverages(
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<List<BeverageResponse>> {
        val beverages = beverageService.getAllBeverages(vendorDetails)
        return ResponseEntity(beverages.map { it.toResponse() }, HttpStatus.OK)
    }
}