package com.qinet.feastique.model.dto.review

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class ReviewDto(
    var id: UUID?,
    var review: String?,

    @field:NotNull(message = "Rating cannot be empty.")
    var rating: Float,

    @field:NotNull(message = "Order ID cannot be empty.")
    var orderId: UUID,

    val beverageReviewDto: BeverageReviewDto?,
    val dessertReviewDto: DessertReviewDto?,
    val foodReviewDto: FoodReviewDto?,
    val handheldReviewDto: HandheldReviewDto?,
    val vendorReviewDto: VendorReviewDto?
)

data class BeverageReviewDto(
    @field:NotNull(message = "Beverage ID cannot be empty.")
    var beverageId: UUID?
)

data class DessertReviewDto(
    @field:NotNull(message = "Dessert ID cannot be empty.")
    var dessertId: UUID?
)

data class FoodReviewDto(
    @field:NotNull(message = "Food ID cannot be empty.")
    var foodId: UUID
)

data class HandheldReviewDto(
    @field:NotNull(message = "Handheld ID cannot be empty.")
    var handheldId: UUID?
)

data class VendorReviewDto(
    @field:NotNull(message = "Handheld ID cannot be empty.")
    var vendorId: UUID?
)

data class ReviewTypeDto(
    @field:NotNull(message = "Review ID cannot be empty.")
    var reviewType: String?
)

