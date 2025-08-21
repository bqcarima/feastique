package com.qinet.feastique.controller.vendor

import com.qinet.feastique.common.mapper.toMinimalResponse
import com.qinet.feastique.model.dto.PasswordDto
import com.qinet.feastique.model.dto.vendor.VendorUpdateDto
import com.qinet.feastique.response.vendor.VendorMinimalResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.vendor.VendorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendors/{vendorId}/account")
class VendorController(
    private val vendorService: VendorService
) {
    @PutMapping("/profile")
    fun updateProfile(
        @PathVariable vendorId: Long,
        @RequestBody @Valid vendorUpdateDto: VendorUpdateDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<Any?> {
        val tokenPair = vendorService.updateVendor(vendorUpdateDto, vendorDetails)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }

    @PostMapping("/password")
    fun changePassword(
        @PathVariable vendorId: Long,
        @RequestBody @Valid passwordDto: PasswordDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        vendorService.changePassword(passwordDto, vendorDetails)
        return ResponseEntity("Password changed successfully.", HttpStatus.OK)
    }

    @GetMapping("/me")
    fun getAccountDetails(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<VendorMinimalResponse> {
        val vendor = vendorService.getVendorByIdWithAddressAndPhoneNumber(vendorDetails)
        return ResponseEntity(vendor.toMinimalResponse(), HttpStatus.OK)
    }
}