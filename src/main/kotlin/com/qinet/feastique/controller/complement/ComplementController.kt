package com.qinet.feastique.controller.complement

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.ComplementDto
import com.qinet.feastique.model.entity.complement.Complement
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.complement.ComplementService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.lang.Exception

@RestController
@RequestMapping("/api/vendor/{vendorId}/complement")
class ComplementController(
    private val complementService: ComplementService
) {

    @PostMapping("/add")
    fun addComplement(
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid complementDto: ComplementDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ComplementResponse {
        val complement = complementService.addComplement(complementDto, vendorDetails)
        return complement.toResponse()
    }

    @DeleteMapping("/delete/{complementId}")
    fun deleteComplement(
        @PathVariable vendorId: Long,
        @PathVariable complementId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) {
        val complement = complementService.getComplement(complementId)
            .orElseThrow { Exception("Complement not found.") }
            .also {
                if(it.vendor.id != vendorDetails.id) {
                    IllegalArgumentException("You do not have permission to delete complement.")
                }
            }

        complementService.deleteComplement(complement)
    }

    @GetMapping("/all")
    fun getAllComplements(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) : List<Complement> {
        if(vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to view these complements.")
        }
        return complementService.getAllComplements(vendorDetails.id )
    }
}

