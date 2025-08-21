package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.AddOnService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vendors/{vendorId}/add_on")
class AddOnController(
    private val addOnService: AddOnService
) {

    @PutMapping
    fun addAddOn (
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid addOnDto: AddOnDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddOnResponse> {
        val addOn = addOnService.addOrUpdateAddOn(addOnDto, vendorDetails)
        return ResponseEntity(addOn.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteAddOn(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        addOnService.deleteAddOn(id, vendorDetails)
        return ResponseEntity("Add-on deleted successfully.", HttpStatus.NO_CONTENT)
    }

    @GetMapping
    fun getAllAddOns(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<List<AddOnResponse>> {
        val addOns = addOnService.getAllAddOns(vendorDetails)
        return ResponseEntity(addOns.map {  it.toResponse() }, HttpStatus.OK)
    }
}