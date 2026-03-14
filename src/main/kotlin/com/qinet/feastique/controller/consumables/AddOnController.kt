package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.consumables.AddOnDto
import com.qinet.feastique.response.consumables.food.AddOnResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.AddOnService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/add_ons")
class AddOnController(
    private val addOnService: AddOnService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateAddOn (
        @PathVariable vendorId: UUID,
        @RequestBody
        @Valid addOnDto: AddOnDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<AddOnResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val addOn = addOnService.addOrUpdateAddOn(addOnDto, vendorDetails)
        return ResponseEntity(addOn.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteAddOn(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        addOnService.deleteAddOn(id, vendorDetails)
        return ResponseEntity("Add-on deleted successfully.", HttpStatus.NO_CONTENT)
    }

    @GetMapping
    fun getAllAddOns(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<AddOnResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val addOnsPage = addOnService.getAllAddOns(vendorDetails, page, size)
        return ResponseEntity(addOnsPage.toResponse(), HttpStatus.OK)
    }

    @GetMapping("/scroll")
    fun scrollAddOns(
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<WindowResponse<AddOnResponse>> {
        require(size in 1..50) { "Page size must be between 1 and 20." }

        securityUtility.validatePath(vendorId, vendorDetails)
        val window = addOnService.scrollHandhelds(vendorDetails, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }
}

