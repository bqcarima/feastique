package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.DiscountDto
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.DiscountService
import jakarta.validation.Valid
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

    ): DiscountResponse {
        return discountService.addOrUpdateDiscount(discountDto, vendorDetails)
    }

    @GetMapping("/all")
    fun getAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): List<DiscountResponse> {
        return discountService.getAllDiscounts(vendorId, vendorDetails).map { it.toResponse() }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteDiscount(
        @PathVariable vendorId: Long,
        @PathVariable id: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) {
        discountService.deleteDiscount(id, vendorId, vendorDetails)
    }

    @DeleteMapping("/delete/all")
    fun deleteAllDiscounts(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) {
        discountService.deleteAllDiscounts(vendorId, vendorDetails)
    }
}