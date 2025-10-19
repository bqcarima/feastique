package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.ComplementService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/vendors/{vendorId}/complements")
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
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<List<ComplementResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val complements = complementService.getAllComplements(vendorDetails)
        return ResponseEntity(complements.map { it.toResponse() }, HttpStatus.OK)
    }
}