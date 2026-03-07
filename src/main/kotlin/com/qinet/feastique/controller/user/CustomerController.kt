package com.qinet.feastique.controller.user

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.user.CustomerUpdateDto
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.response.user.CustomerResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.customer.CustomerService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/customers/{customerId}/account")
class CustomerController(
    private val customerService: CustomerService,
    private val securityUtility: SecurityUtility,
) {

    @PatchMapping("/profile")
    fun updateProfile(
        @PathVariable customerId: UUID,
        @RequestBody @Valid customerUpdateDto: CustomerUpdateDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<TokenPairResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val tokenPairResponse = customerService.updateCustomer(customerUpdateDto, customerDetails)
        return ResponseEntity(tokenPairResponse, HttpStatus.OK)
    }

    @PatchMapping("/password")
    fun changePassword(
        @PathVariable customerId: UUID,
        @RequestBody @Valid passwordChangeDto: PasswordChangeDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        customerService.changePassword(passwordChangeDto, customerDetails)
        return ResponseEntity("Password changed successfully.", HttpStatus.OK)
    }

    @GetMapping("/me")
    fun getAccountDetails(
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CustomerResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val customer = customerService.getCustomerWithPhoneNumberAndAddress(customerDetails)
        return ResponseEntity(customer.toResponse(), HttpStatus.OK)
    }
}

