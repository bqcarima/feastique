package com.qinet.feastique.controller.bookmark

import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.response.consumables.beverage.BeverageResponse
import com.qinet.feastique.response.consumables.dessert.DessertResponse
import com.qinet.feastique.response.consumables.food.FoodResponse
import com.qinet.feastique.response.consumables.handheld.HandheldResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.user.VendorBookmarkResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.bookmark.BookmarkService
import com.qinet.feastique.utility.SecurityUtility
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/v1/customers/{customerId}")
class BookmarkController(
    private val bookmarkService: BookmarkService,
    private val securityUtility: SecurityUtility,
) {

    @PostMapping("/beverages/{beverageId}/bookmark")
    fun bookmarkBeverage(
        @PathVariable customerId: UUID,
        @PathVariable beverageId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<Void> {
        securityUtility.validatePath(customerId, userDetails)
        bookmarkService.bookmarkOrUnbookmarkBeverage(beverageId, userDetails)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/desserts/{dessertId}/bookmark")
    fun bookmarkDessert(
        @PathVariable customerId: UUID,
        @PathVariable dessertId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<Void> {
        securityUtility.validatePath(customerId, userDetails)
        bookmarkService.bookmarkOrUnbookmarkDessert(dessertId, userDetails)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/foods/{foodId}/bookmark")
    fun bookmarkFood(
        @PathVariable customerId: UUID,
        @PathVariable foodId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<Void> {
        securityUtility.validatePath(customerId, userDetails)
        bookmarkService.bookmarkOrUnbookmarkFood(foodId, userDetails)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/handhelds/{handheldId}/bookmark")
    fun bookmarkHandheld(
        @PathVariable customerId: UUID,
        @PathVariable handheldId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<Void> {
        securityUtility.validatePath(customerId, userDetails)
        bookmarkService.bookmarkOrUnbookmarkHandheld(handheldId, userDetails)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/vendors/{vendorId}/bookmark")
    fun bookmarkVendor(
        @PathVariable customerId: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<Void> {
        securityUtility.validatePath(customerId, userDetails)
        bookmarkService.bookmarkOrUnbookmarkVendor(vendorId, userDetails)
        return ResponseEntity(HttpStatus.OK)
    }

    // Fetch endpoints
    @GetMapping("/bookmarks/beverages")
    fun scrollBeverageBookmarks(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<WindowResponse<BeverageResponse>> {
        securityUtility.validatePath(customerId, userDetails)
        val bookmarks = bookmarkService.scrollBeverageBookmarks(cursor, size, userDetails)
        return ResponseEntity(bookmarks, HttpStatus.OK)
    }

    @GetMapping("/bookmarks/desserts")
    fun scrollDessertBookmarks(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<WindowResponse<DessertResponse>> {
        securityUtility.validatePath(customerId, userDetails)
        val bookmarks = bookmarkService.scrollDessertBookmarks(cursor, size, userDetails)
        return ResponseEntity(bookmarks, HttpStatus.OK)
    }

    @GetMapping("/bookmarks/foods")
    fun scrollFoodBookmarks(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<WindowResponse<FoodResponse>> {
        securityUtility.validatePath(customerId, userDetails)
        val bookmarks = bookmarkService.scrollFoodBookmarks(cursor, size, userDetails)
        return ResponseEntity(bookmarks, HttpStatus.OK)
    }

    @GetMapping("/bookmarks/handhelds")
    fun scrollHandheldBookmarks(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<WindowResponse<HandheldResponse>> {
        securityUtility.validatePath(customerId, userDetails)
        val bookmarks = bookmarkService.scrollHandheldBookmarks(cursor, size, userDetails)
        return ResponseEntity(bookmarks, HttpStatus.OK)
    }

    @GetMapping("/bookmarks/vendors")
    fun scrollVendorBookmarks(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        @AuthenticationPrincipal userDetails: UserSecurity,
    ): ResponseEntity<WindowResponse<VendorBookmarkResponse>> {
        securityUtility.validatePath(customerId, userDetails)
        val bookmarks = bookmarkService.scrollVendorBookmarks(cursor, size, userDetails)
        return ResponseEntity(bookmarks, HttpStatus.OK)
    }
}

