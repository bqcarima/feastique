package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.consumables.ComplementDto
import com.qinet.feastique.response.consumables.food.ComplementResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.ComplementService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/complements")
class ComplementController(
    private val complementService: ComplementService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateComplement(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid complementDto: ComplementDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<ComplementResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val complement = complementService.addOrUpdateComplement(complementDto, vendorDetails)
        return ResponseEntity(complement.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteComplement(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        complementService.deleteComplement(id, vendorDetails)
        return ResponseEntity("Complement deleted successfully.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getComplement(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<ComplementResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val complement = complementService.getComplement(id, vendorDetails)
        return ResponseEntity(complement.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllComplements(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<ComplementResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val complementsPage = complementService.getAllComplements(vendorDetails, page, size)
        return ResponseEntity(complementsPage.toResponse() , HttpStatus.OK)
    }

    @GetMapping("/scroll")
    fun scrollComplements(
        @PathVariable(required = false) vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<WindowResponse<ComplementResponse>> {
        require(size in 1..50) { "Page size must be between 1 and 20." }

        securityUtility.validatePath(vendorId, vendorDetails)
        val window = complementService.scrollComplements(vendorDetails, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }
}

