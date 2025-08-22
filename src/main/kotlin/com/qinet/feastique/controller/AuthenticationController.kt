package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.LogoutDto
import com.qinet.feastique.model.dto.customer.SignupDto
import com.qinet.feastique.model.dto.vendor.VendorSignupDto
import com.qinet.feastique.response.CustomerResponse
import com.qinet.feastique.response.vendor.VendorResponse
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.service.AuthenticationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {
    @PostMapping("/signup")
    fun signup(@RequestBody @Valid signupDto: SignupDto): ResponseEntity<CustomerResponse> {
        val customer = authenticationService.handleCustomerSignup(signupDto)
        return ResponseEntity(customer.toResponse(), HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDto: LoginDto): TokenPairResponse {
        return authenticationService.handleCustomerLogin(loginDto)
    }

    @PostMapping("/vendors/signup")
    fun vendorSignup(@RequestBody @Valid vendorSignupDto: VendorSignupDto): ResponseEntity<VendorResponse> {
        val vendor = authenticationService.handleVendorSignup(vendorSignupDto)
        return ResponseEntity(vendor.toResponse(), HttpStatus.CREATED)
    }


    @PostMapping("/vendors/login")
    fun vendorLogin(
        @RequestBody @Valid loginDto: LoginDto): TokenPairResponse {
        return authenticationService.handleVendorLogin(loginDto)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshRequestDto: RefreshRequestDto): ResponseEntity<AccessTokenResponse> {
        val accessTokenResponse = authenticationService.handleRefresh(refreshRequestDto.refreshToken.trim())
        return ResponseEntity.ok(accessTokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization", required = false) authorizationHeader: String?,
        @RequestBody(required = false) logoutRequestDto: LogoutDto?
    ): ResponseEntity<Map<String, String>> {
        authenticationService.handleLogout(authorizationHeader, logoutRequestDto)
        return ResponseEntity.ok(mapOf("message" to "Successfully logged out"))
    }
    data class RefreshRequestDto(val refreshToken: String)
}

