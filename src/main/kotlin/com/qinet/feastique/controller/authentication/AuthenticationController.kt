package com.qinet.feastique.controller.authentication

import com.qinet.feastique.common.mapper.toMinimalResponse
import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.LogoutDto
import com.qinet.feastique.model.dto.user.CustomerSignupDto
import com.qinet.feastique.model.dto.user.VendorSignupDto
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.response.user.CustomerResponse
import com.qinet.feastique.response.user.VendorMinimalResponse
import com.qinet.feastique.service.authentication.AuthenticationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {
    @PostMapping("/signup")
    fun signup(@RequestBody @Valid customerSignupDto: CustomerSignupDto): ResponseEntity<CustomerResponse> {
        val customer = authenticationService.handleCustomerSignup(customerSignupDto)
        return ResponseEntity(customer.toResponse(), HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDto: LoginDto): TokenPairResponse {
        return authenticationService.handleCustomerLogin(loginDto)
    }

    @PostMapping("/vendors/signup")
    fun vendorSignup(@RequestBody @Valid vendorSignupDto: VendorSignupDto): ResponseEntity<VendorMinimalResponse> {
        val vendor = authenticationService.handleVendorSignup(vendorSignupDto)
        return ResponseEntity(vendor.toMinimalResponse(), HttpStatus.CREATED)
    }


    @PostMapping("/vendors/login")
    fun vendorLogin(
        @RequestBody @Valid loginDto: LoginDto
    ): TokenPairResponse {
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