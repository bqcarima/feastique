package com.qinet.feastique.controller.review

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.review.ReviewTypeDto
import com.qinet.feastique.model.dto.review.ReviewDto
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.review.BaseReviewResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.review.ReviewService
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class ReviewController(
    private val securityUtility: SecurityUtility,
    private val reviewService: ReviewService
) {

    @GetMapping("/vendors/{vendorId}/{itemId}/reviews")
    fun getAllItemReviews(
        @PathVariable itemId: UUID,
        @PathVariable vendorId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestBody reviewTypeDto: ReviewTypeDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): ResponseEntity<PageResponse<BaseReviewResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val page = reviewService.getAllItemReviews(itemId, reviewTypeDto, page, size)
        return ResponseEntity(page.toResponse(), HttpStatus.OK)
    }

    // Scroll reviews
    @GetMapping(
        path = [
            "/customers/{customerId}/beverages/{beverageId}/reviews/scroll",
            "/vendors/{vendorId}/beverages/{beverageId}/reviews/scroll"
        ]
    )
    fun scrollBeverageReviews(
        @PathVariable beverageId: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BaseReviewResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val window = reviewService.scrollBeverageReviews(beverageId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/desserts/{dessertId}/reviews/scroll",
            "/vendors/{vendorId}/desserts/{dessertId}/reviews/scroll"
        ]
    )
    fun scrollDessertReviews(
        @PathVariable dessertId: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BaseReviewResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val window = reviewService.scrollDessertReviews(dessertId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/foods/{foodId}/reviews/scroll",
            "/vendors/{vendorId}/foods/{foodId}/reviews/scroll"
        ]
    )
    fun scrollFoodReviews(
        @PathVariable foodId: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BaseReviewResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val window = reviewService.scrollFoodReviews(foodId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/handhelds/{handheldId}/reviews/scroll",
            "/vendors/{vendorId}/handhelds/{handheldId}/reviews/scroll"
        ]
    )
    fun scrollHandheldReviews(
        @PathVariable handheldId: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BaseReviewResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val window = reviewService.scrollHandheldReviews(handheldId, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/reviews/scroll",
            "/vendors/{vendorId}/reviews/scroll"
        ]
    )
    fun scrollVendorReviews(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = true) vendorId: UUID?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ): ResponseEntity<WindowResponse<BaseReviewResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val window = reviewService.scrollVendorReviews(vendorId!!, cursor, size)
        return ResponseEntity(window, HttpStatus.OK)
    }

    @PutMapping("/customers/{customerId}/reviews")
    fun addOrUpdateReview(
        @PathVariable customerId: UUID,
        @RequestBody reviewDto: ReviewDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ): ResponseEntity<BaseReviewResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val updatedReview = reviewService.addOrUpdateReview(reviewDto, customerDetails)
        return ResponseEntity(updatedReview.toResponse(), HttpStatus.OK)
    }

    // delete review endpoints
    @DeleteMapping("/customers/{customerId}/reviews/delete/{id}")
    fun deleteReview(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @RequestBody reviewTypeDto: ReviewTypeDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ): ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        reviewService.deleteReview(reviewTypeDto, id, customerDetails)
        return ResponseEntity("${reviewTypeDto.reviewType} deleted", HttpStatus.OK)
    }
}

