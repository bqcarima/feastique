package com.qinet.feastique.controller.like

import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.like.LikeService
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/customers/{customerId}/vendors")
class LikeController(
    private val likeService: LikeService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping("/{vendorId}/beverages/{beverageId}/like")
    fun likeOrUnlikeBeverage(
        @PathVariable beverageId: UUID,
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikeBeverage(beverageId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }

    @PutMapping("/{vendorId}/desserts/{dessertId}/like")
    fun likeOrUnlikeDessert(
        @PathVariable dessertId: UUID,
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikeDessert(dessertId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }

    @PutMapping("/{vendorId}/foods/{foodId}/like")
    fun likeOrUnlikeFood(
        @PathVariable foodId: UUID,
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikeFood(foodId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }

    @PutMapping("/{vendorId}/handhelds/{handheldId}/like")
    fun likeOrUnlikeHandheld(
        @PathVariable handheldId: UUID,
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikeHandheld(handheldId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }

    @PutMapping("/{vendorId}/posts/{postId}/like")
    fun likeOrUnlikePost(
        @PathVariable postId: UUID,
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikePost(postId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }

    @PutMapping("/{vendorId}/like")
    fun likeOrUnlikeVendor(
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        likeService.likeOrUnlikeVendor(vendorId, customerDetails)
        return ResponseEntity("Completed", HttpStatus.OK)
    }
}

