package com.qinet.feastique.controller.address

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.address.AddressDto
import com.qinet.feastique.response.user.CustomerAddressResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.customer.CustomerAddressService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers/{customerId}/address")
class CustomerAddressController(
    private val customerAddressService: CustomerAddressService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateAddress(
        @PathVariable customerId: UUID,
        @RequestBody @Valid addressDto: AddressDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<CustomerAddressResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        val address = customerAddressService.addAddress(addressDto, customerDetails)
        return ResponseEntity(address.map { it.toResponse() }, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteAddress(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        customerAddressService.deleteAddress(id, customerDetails)
        return ResponseEntity("Address deleted successfully.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAddress(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CustomerAddressResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val address = customerAddressService.getAddressById(id, customerDetails)
        return ResponseEntity(address.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAddresses(
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<List<CustomerAddressResponse>> {
        securityUtility.validatePath(customerId, customerDetails)
        val addresses = customerAddressService.getAllAddresses(customerDetails)
        return ResponseEntity(addresses.map { it.toResponse() }, HttpStatus.OK)
    }
}

