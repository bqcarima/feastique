package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.OrderItemDto
import com.qinet.feastique.response.order.CartResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.order.CartService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/customers/{customerId}/cart")
class CartController(
    private val cartService: CartService,
    private val securityUtility: SecurityUtility
) {

    @GetMapping
    fun getCart(
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CartResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val cart = cartService.getCart(customerDetails)
        return ResponseEntity(cart?.toResponse(), HttpStatus.OK)
    }
    @PutMapping
    fun addOrUpdateCartItem(
        @PathVariable customerId: UUID,
        @RequestBody orderItemDto: OrderItemDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CartResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val cart = cartService.addItemToCart(orderItemDto, customerDetails)
        return ResponseEntity(cart.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun removeCartItem(
        @PathVariable id: UUID,
        @RequestBody @Valid cartItemDto: CartItemDto,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        cartService.removeItems(cartItemDto, customerDetails)
        return ResponseEntity("Item removed from cart.", HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteCart(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        cartService.deleteCart(customerDetails)
        return ResponseEntity("Cart cleared.", HttpStatus.OK)
    }

    @PutMapping("/increase/{id}")
    fun increaseQuantity(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        cartService.increaseItemQuantity(id, customerDetails)
        return ResponseEntity("Item quantity increased,", HttpStatus.OK)
    }

    @PutMapping("/reduce/{id}")
    fun reduceQuantity(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        cartService.reduceItemQuantity(id, customerDetails)
        return ResponseEntity("Item quantity reduced,", HttpStatus.OK)
    }
}

