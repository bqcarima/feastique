package com.qinet.feastique.controller

import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.media.MediaService
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/v1")
class MediaController(
    private val mediaService: MediaService,
    private val securityUtility: SecurityUtility
) {

    @PostMapping("/customers/{customerId}/display-picture")
    fun updateCustomerDisplayPicture(
        @PathVariable customerId: UUID,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal customerDetails: UserSecurity
    ): ResponseEntity<Map<String, String>> {
        securityUtility.validatePath(customerId, customerDetails)
        val imageUrl = mediaService.updateCustomerDisplayPicture(customerDetails, file)
        return ResponseEntity(mapOf("imageUrl" to imageUrl), HttpStatus.OK)
    }

    @PostMapping("/vendors/{vendorId}/display-picture")
    fun updateVendorDisplayPicture(
        @PathVariable vendorId: UUID,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<Map<String, String>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val imageUrl = mediaService.updateVendorDisplayPicture(vendorDetails, file)
        return ResponseEntity(mapOf("imageUrl" to imageUrl), HttpStatus.OK)
    }

    @PostMapping("/vendors/{vendorId}/preview-images")
    fun addVendorPreviewImage(
        @PathVariable vendorId: UUID,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<Map<String, String>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val imageUrl = mediaService.addVendorPreviewImage(vendorDetails, file)
        return ResponseEntity(mapOf("imageUrl" to imageUrl), HttpStatus.CREATED)
    }

    @DeleteMapping("/vendors/{vendorId}/preview-images/{imageId}")
    fun removeVendorPreviewImage(
        @PathVariable vendorId: UUID,
        @PathVariable imageId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<Void> {
        securityUtility.validatePath(vendorId, vendorDetails)
        mediaService.removeVendorPreviewImage(vendorDetails, imageId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/vendors/{vendorId}/preview-images")
    fun getVendorPreviewImages(
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ): ResponseEntity<Set<String>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val images = mediaService.getVendorPreviewImages(vendorDetails)
        return ResponseEntity.ok(images)
    }
}

