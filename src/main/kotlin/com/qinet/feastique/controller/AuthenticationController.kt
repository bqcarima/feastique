package com.qinet.feastique.controller

import com.qinet.feastique.model.dto.LoginDTO
import com.qinet.feastique.model.dto.SignupDTO
import com.qinet.feastique.model.dto.VendorSignupDTO
import com.qinet.feastique.response.Token
import com.qinet.feastique.response.TokenPair
import com.qinet.feastique.service.CustomerService
import com.qinet.feastique.service.VendorService
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
@RequestMapping("/auth")
class AuthenticationController(
    val customerService: CustomerService,
    private val vendorService: VendorService,
    private val jwtUtility: JwtUtility
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody
        @Valid
        signupDTO: SignupDTO
    ): ResponseEntity<String> {
        customerService.signupCustomer(signupDTO)
        return ResponseEntity("Created", HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDTO: LoginDTO): TokenPair {
        return customerService.login(loginDTO)
    }

    @PostMapping("/vendor/signup")
    fun vendorSignup(
        @RequestBody
        @Valid
        vendorSignupDTO: VendorSignupDTO
        ): ResponseEntity<String> {
            vendorService.signup(vendorSignupDTO)
            return ResponseEntity("Created", HttpStatus.CREATED)
        }


    @PostMapping("/vendor/login")
    fun vendorLogin(
        @RequestBody
        @Valid
        loginDTO: LoginDTO): TokenPair {
        return vendorService.login(loginDTO)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshToken: RefreshRequest): Token {
        val rawRefreshToken = refreshToken.refreshToken.trim()
        return jwtUtility.refresh(rawRefreshToken)
    }

}