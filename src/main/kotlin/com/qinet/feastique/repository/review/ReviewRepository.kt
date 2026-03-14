package com.qinet.feastique.repository.review

import com.qinet.feastique.model.entity.review.*
import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface BeverageReviewRepository : JpaRepository<BeverageReview, UUID> {
    fun findAllByBeverageId(beverageId: UUID, pageable: Pageable): Page<BeverageReview>
    fun findAllByBeverageId(beverageId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<BeverageReview>
    fun existsByBeverageIdAndCustomerIdAndOrderId(beverageId: UUID, customerId: UUID, orderId: UUID): Boolean
}

@Repository
interface DessertReviewRepository : JpaRepository<DessertReview, UUID> {
    fun findAllByDessertId(dessertId: UUID, pageable: Pageable): Page<DessertReview>
    fun findAllByDessertId(dessertId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<DessertReview>
    fun existsByDessertIdAndCustomerIdAndOrderId(dessertId: UUID, customerId: UUID, orderId: UUID): Boolean
}

@Repository
interface FoodReviewRepository : JpaRepository<FoodReview, UUID> {
    fun findAllByFoodId(foodId: UUID, pageable: Pageable): Page<FoodReview>
    fun findAllByFoodId(foodId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<FoodReview>
    fun existsByFoodIdAndCustomerIdAndOrderId(foodId: UUID, customerId: UUID, orderId: UUID): Boolean
}

@Repository
interface HandheldReviewRepository : JpaRepository<HandheldReview, UUID> {
    fun findAllByHandheldId(handheldId: UUID, pageable: Pageable): Page<HandheldReview>
    fun findAllByHandheldId(handheld: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<HandheldReview>
    fun existsByHandheldIdAndCustomerIdAndOrderId(handheldId: UUID, customerId: UUID, orderId: UUID): Boolean
}

@Repository
interface VendorReviewRepository : JpaRepository<VendorReview, UUID> {
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<VendorReview>
    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<VendorReview>
    fun existsByVendorIdAndCustomerIdAndOrderId(vendorId: UUID, customerId: UUID, orderId: UUID): Boolean
}

