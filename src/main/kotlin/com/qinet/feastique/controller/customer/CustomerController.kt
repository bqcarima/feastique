package com.qinet.feastique.controller.customer

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.PasswordDto
import com.qinet.feastique.model.dto.customer.UpdateDto
import com.qinet.feastique.response.CustomerResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.customer.CustomerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers/{customerId}/account")
class CustomerController(
    private val customerService: CustomerService,
) {

    @PutMapping("/profile")
    fun updateProfile(
        @PathVariable("customerId") customerId: Long,
        @RequestBody @Valid updateDto: UpdateDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<Any?> {
        val response = customerService.updateCustomer(updateDto, customerDetails)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("/password")
    fun changePassword(
        @PathVariable("customerId") customerId: Long,
        @RequestBody @Valid passwordDto: PasswordDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        customerService.changePassword(passwordDto, customerDetails)
        return ResponseEntity("Password changed successfully.", HttpStatus.OK)
    }

    @GetMapping("/me")
    fun getAccountDetails(
        @PathVariable("customerId") customerId: Long,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CustomerResponse> {
        val customer = customerService.getCustomerWithPhoneNumberAndAddress(customerDetails)
        return ResponseEntity(customer.toResponse(), HttpStatus.OK)
    }

}