package com.qinet.feastique.controller.consumables

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.HandheldAvailabilityDto
import com.qinet.feastique.model.dto.consumables.HandheldDto
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.consumables.HandheldService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*


/**
 * Controller for managing handhelds. Provides endpoints for adding/updating, deleting, retrieving, and toggling availability of handhelds.
 * All endpoints are secured and require the vendor to be authenticated. The controller uses the HandheldService to perform business logic
 * and the SecurityUtility to validate that the authenticated vendor is authorized to perform actions on the specified vendorId.
 */

@RestController
@RequestMapping("/api/v1")
class HandheldController(
    private val handheldService: HandheldService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping("/vendors/{vendorId}/handhelds")
    fun addOrUpdateHandheld(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid handheldDto: HandheldDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<HandheldResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheld = handheldService.addOrUpdateHandheld(handheldDto, vendorDetails)
        return ResponseEntity(handheld.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/vendors/{vendorId}/handhelds/{id}")
    fun deleteHandheld(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        handheldService.deleteHandheld(id, vendorDetails)
        return ResponseEntity("Handheld deleted successfully. All relationships will be deleted as well.", HttpStatus.OK)
    }

    @GetMapping("/vendors/{vendorId}/handhelds/{id}")
    fun getHandheld(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<HandheldResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val handheld = handheldService.getHandheldById(id, vendorDetails)
        return ResponseEntity(handheld.toResponse(), HttpStatus.OK)
    }
    @GetMapping("/vendors/{vendorId}/handhelds")
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

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/handhelds/scroll",
            "/vendors/{vendorId}/handhelds/scroll"
        ]
    )
    fun scrollHandhelds(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<WindowResponse<HandheldResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val window = handheldService.scrollHandhelds(vendorId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @PutMapping("/vendors/{vendorId}/handhelds/availability/{id}")
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

