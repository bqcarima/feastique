package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.response.PageResponse
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.HandheldService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


/**
 * Controller for managing handhelds. Provides endpoints for adding/updating, deleting, retrieving, and toggling availability of handhelds.
 * All endpoints are secured and require the vendor to be authenticated. The controller uses the HandheldService to perform business logic
 * and the SecurityUtility to validate that the authenticated vendor is authorized to perform actions on the specified vendorId.
 */

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/handhelds")
class HandheldController(
    private val handheldService: HandheldService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdateHandheld(
        @PathVariable vendorId: UUID,
        @RequestBody
        @Valid handheldDto: HandheldDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<HandheldResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheld = handheldService.addOrUpdateHandheld(handheldDto, vendorDetails)
        return ResponseEntity(handheld.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteHandheld(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        handheldService.deleteHandheld(id, vendorDetails)
        return ResponseEntity("Handheld deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getHandheld(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<HandheldResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheld = handheldService.getHandheldById(id, vendorDetails)
        return ResponseEntity(handheld.toResponse(), HttpStatus.OK)
    }
    @GetMapping
    fun getAllHandhelds(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PageResponse<HandheldResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheldsPage = handheldService.getAllHandhelds(vendorDetails, page, size)
        return ResponseEntity(handheldsPage.toResponse(), HttpStatus.OK)
    }

    @PutMapping("/availability/{id}")
    fun toggleHandheldAvailability(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @RequestBody @Valid handheldAvailabilityDto: HandheldAvailabilityDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<HandheldResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheld = handheldService.toggleAvailability(handheldAvailabilityDto, id, vendorDetails)
        return ResponseEntity(handheld.toResponse(), HttpStatus.OK)
    }
}