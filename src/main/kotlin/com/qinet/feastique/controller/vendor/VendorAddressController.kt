package com.qinet.feastique.controller.vendor

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.AddressDto
import com.qinet.feastique.response.AddressResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.vendor.VendorAddressService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/address")
class VendorAddressController(
    private val vendorAddressService: VendorAddressService,
) {
    @PostMapping("/add")
    fun updateAddress(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid addressDto: AddressDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddressResponse> {
        val address = vendorAddressService.updateAddress(addressDto, vendorDetails)
        return ResponseEntity(address.toResponse(), HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getAddress(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddressResponse> {
        val address = vendorAddressService.getAddress(vendorId, vendorDetails)
        return ResponseEntity(address.toResponse(), HttpStatus.OK)
    }
}

