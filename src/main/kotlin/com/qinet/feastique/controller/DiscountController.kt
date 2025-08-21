package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DiscountDto
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.DiscountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vendor/{vendorId}/discount")
class DiscountController(
    private val discountService: DiscountService
) {

    @PostMapping("/add")
    fun addOrUpdateDiscount(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid discountDto: DiscountDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<DiscountResponse> {
        val discount =  discountService.addOrUpdateDiscount(discountDto, vendorDetails)
        return ResponseEntity(discount.toResponse(), HttpStatus.CREATED)
    }

    @GetMapping("/all")
    fun getAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<List<DiscountResponse>> {
        val discounts =  discountService.getAllDiscounts(vendorDetails)
        return ResponseEntity(discounts.map { it.toResponse() }, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteDiscount(
        @PathVariable vendorId: Long,
        @PathVariable id: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        discountService.deleteDiscount(id, vendorDetails)
        return ResponseEntity("Discount deleted successfully." ,HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/delete/all")
    fun deleteAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        discountService.deleteAllDiscounts(vendorDetails)
        return ResponseEntity("Discount deleted successfully." ,HttpStatus.NO_CONTENT)
    }
}

