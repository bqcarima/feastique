package com.qinet.feastique.controller

import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.SignupDto
import com.qinet.feastique.model.dto.VendorSignupDto
import com.qinet.feastique.response.TokenResponse
import com.qinet.feastique.response.TokenPair
import com.qinet.feastique.service.customer.CustomerService
import com.qinet.feastique.service.vendor.VendorService
import com.qinet.feastique.utility.JwtUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RefreshRequest(val refreshToken: String)

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    val customerService: CustomerService,
    private val vendorService: VendorService,
    private val jwtUtility: JwtUtility
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody
        @Valid
        signupDTO: SignupDto
    ): ResponseEntity<String> {
        customerService.signupCustomer(signupDTO)
        return ResponseEntity("Created", HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDto: LoginDto): TokenPair {
        return customerService.login(loginDto)
    }

    @PostMapping("/vendor/signup")
    fun vendorSignup(
        @RequestBody
        @Valid
        vendorSignupDTO: VendorSignupDto
        ): ResponseEntity<String> {
            vendorService.signup(vendorSignupDTO)
            return ResponseEntity("Created", HttpStatus.CREATED)
        }


    @PostMapping("/vendor/login")
    fun vendorLogin(
        @RequestBody
        @Valid
        loginDto: LoginDto): TokenPair {
        return vendorService.login(loginDto)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshToken: RefreshRequest): TokenResponse {
        val rawRefreshToken = refreshToken.refreshToken.trim()
        return jwtUtility.refresh(rawRefreshToken)
    }

}