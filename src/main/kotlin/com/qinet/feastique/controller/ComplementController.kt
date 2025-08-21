package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.ComplementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vendor/{vendorId}/complement")
class ComplementController(private val complementService: ComplementService) {

    @PostMapping("/add")
    fun addOrUpdateComplement(
        @PathVariable vendorId: Long,
        @RequestBody @Valid complementDto: ComplementDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<ComplementResponse> {
        val complement = complementService.addOrUpdateComplement(complementDto, vendorDetails)
        return ResponseEntity(complement.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteComplement(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        complementService.deleteComplement(id, vendorDetails)
        return ResponseEntity("Complement deleted successfully.",HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getComplement(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<ComplementResponse> {
        val complement = complementService.getComplement(id, vendorDetails)
        return ResponseEntity(complement.toResponse(), HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAllComplements(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<List<ComplementResponse>> {
        val complements = complementService.getAllComplements(vendorDetails)
        return ResponseEntity(complements.map { it.toResponse() }, HttpStatus.OK)
    }
}