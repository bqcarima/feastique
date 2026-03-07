package com.qinet.feastique.controller.contact

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.contact.PhoneNumberDto
import com.qinet.feastique.model.entity.contact.CustomerPhoneNumber
import com.qinet.feastique.response.user.PhoneNumberResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.contact.PhoneNumberService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers/{customerId}/numbers")
class CustomerPhoneNumberController(
    private val phoneNumberService: PhoneNumberService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdatePhoneNumber(
        @PathVariable("customerId") customerId: UUID,
        @RequestBody @Valid phoneNumberDto: PhoneNumberDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<PhoneNumberResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        val phoneNumbers = phoneNumberService.addOrUpdatePhoneNumber(phoneNumberDto, customerDetails)
        return ResponseEntity(phoneNumbers.map { it.toResponse() }, HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deletePhoneNumber(
        @PathVariable("id") id: UUID,
        @PathVariable("customerId") customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        phoneNumberService.deletePhoneNumber(id, customerDetails)
        return ResponseEntity("Phone number deleted successfully.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getPhoneNumber(
        @PathVariable("id") id: UUID,
        @PathVariable("customerId") customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<PhoneNumberResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val phoneNumber = phoneNumberService.getPhoneNumber<CustomerPhoneNumber>(id, customerDetails)
        return ResponseEntity(phoneNumber.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllPhoneNumbers(
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<PhoneNumberResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        val phoneNumbers = phoneNumberService.getAllPhoneNumbers<CustomerPhoneNumber>(customerDetails)
        return ResponseEntity(phoneNumbers.map { it.toResponse() }, HttpStatus.OK)
    }
}