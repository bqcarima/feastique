package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DiscountDto
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.DiscountService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendors/{vendorId}/discounts")
class DiscountController(
    private val discountService: DiscountService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateDiscount(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid discountDto: DiscountDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<DiscountResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val discount =  discountService.addOrUpdateDiscount(discountDto, vendorDetails)
        return ResponseEntity(discount.toResponse(), HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getDiscount(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<DiscountResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val discount = discountService.getDiscount(id, vendorDetails)
        return ResponseEntity(discount.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<List<DiscountResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val discounts =  discountService.getAllDiscounts(vendorDetails)
        return ResponseEntity(discounts.map { it.toResponse() }, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteDiscount(
        @PathVariable vendorId: Long,
        @PathVariable id: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        discountService.deleteDiscount(id, vendorDetails)
        return ResponseEntity("Discount deleted successfully." ,HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/all")
    fun deleteAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        discountService.deleteAllDiscounts(vendorDetails)
        return ResponseEntity("Discount deleted successfully." ,HttpStatus.NO_CONTENT)
    }
}

