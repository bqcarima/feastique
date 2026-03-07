package com.qinet.feastique.controller.address

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.address.AddressDto
import com.qinet.feastique.response.user.AddressResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.vendor.VendorAddressService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/vendor/{vendorId}/address")
class VendorAddressController(
    private val vendorAddressService: VendorAddressService,
    private val securityUtility: SecurityUtility,
) {
    @PostMapping("/add")
    fun updateAddress(
        @PathVariable vendorId: UUID,
        @RequestBody
        @Valid addressDto: AddressDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddressResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val address = vendorAddressService.updateAddress(addressDto, vendorDetails)
        return ResponseEntity(address.toResponse(), HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getAddress(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddressResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val address = vendorAddressService.getAddress(vendorId, vendorDetails)
        return ResponseEntity(address.toResponse(), HttpStatus.OK)
    }
}