package com.qinet.feastique.controller.phoneNumber

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.PhoneNumberDto
import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import com.qinet.feastique.response.PhoneNumberResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.PhoneNumberService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers/{customerId}/numbers")
class CustomerPhoneNumberController(private val phoneNumberService: PhoneNumberService) {

    @PutMapping
    fun addOrUpdatePhoneNumber(
        @PathVariable("customerId") customerId: Long,
        @RequestBody @Valid phoneNumberDto: PhoneNumberDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<PhoneNumberResponse>> {
        val phoneNumbers = phoneNumberService.addOrUpdatePhoneNumber(phoneNumberDto, customerDetails)
        return ResponseEntity(phoneNumbers.map { it.toResponse() }, HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deletePhoneNumber(
        @PathVariable("id") id: Long,
        @PathVariable("customerId") customerId: Long,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        phoneNumberService.deletePhoneNumber(id, customerDetails)
        return ResponseEntity("Phone number deleted successfully.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getPhoneNumber(
        @PathVariable("id") id: Long,
        @PathVariable("customerId") customerId: Long,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<PhoneNumberResponse> {
        val phoneNumber = phoneNumberService.getPhoneNumber<CustomerPhoneNumber>(id, customerDetails)
        return ResponseEntity(phoneNumber.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllPhoneNumbers(
        @PathVariable customerId: Long,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<PhoneNumberResponse>> {
        val phoneNumbers = phoneNumberService.getAllPhoneNumbers<CustomerPhoneNumber>(customerDetails)
        return ResponseEntity(phoneNumbers.map { it.toResponse() }, HttpStatus.OK)
    }
}