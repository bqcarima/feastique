package com.qinet.feastique.controller.user

import com.qinet.feastique.common.mapper.toMinimalResponse
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.model.dto.user.VendorUpdateDto
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.response.user.VendorMinimalResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.vendor.VendorService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/account")
class VendorController(
    private val vendorService: VendorService,
    private val securityUtility: SecurityUtility
) {
    @PatchMapping("/profile")
    fun updateProfile(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid vendorUpdateDto: VendorUpdateDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<TokenPairResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val tokenPair = vendorService.updateVendor(vendorUpdateDto, vendorDetails)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }

    @PatchMapping("/password")
    fun changePassword(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid passwordChangeDto: PasswordChangeDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        vendorService.changePassword(passwordChangeDto, vendorDetails)
        return ResponseEntity("Password changed successfully.", HttpStatus.OK)
    }

    @GetMapping("/me")
    fun getAccountDetails(
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<VendorMinimalResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val vendor = vendorService.getVendorByIdWithAddressAndPhoneNumber(vendorDetails)
        return ResponseEntity(vendor.toMinimalResponse(), HttpStatus.OK)
    }
}