package com.qinet.feastique.controller.vendor

import com.qinet.feastique.model.dto.AddressDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.vendor.VendorAddressService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/address")
class VendorAddressController(
    private val vendorAddressService: VendorAddressService,
) {

    @PostMapping("/add")
    fun addAddress(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid addressDto: AddressDto
    ): ResponseEntity<String> {
        vendorAddressService.addAddress(addressDto)
        return ResponseEntity("Address Added.", HttpStatus.CREATED)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteById(
        @PathVariable vendorId: Long,
        @PathVariable id: Long,
        @AuthenticationPrincipal
        vendorDetails: UserSecurity
    ): ResponseEntity<String> {

        val vendorAddress = vendorAddressService.getAddress(id).orElseThrow {
            Exception("Address not found.")
        }

        if(vendorAddressService.getAllAddresses(vendorDetails.id).size < 2) throw Exception("Cannot remove all addresses.")

        if(vendorDetails.id == vendorAddress.id) {
            vendorAddressService.deleteAddress(vendorAddress)
        }

        return ResponseEntity("Address deleted", HttpStatus.CREATED)
    }

    @GetMapping("/all")
    fun getAllAddresses(@PathVariable vendorId: Long): List<VendorAddress> {
        return vendorAddressService.getAllAddresses(vendorId)
    }
}

