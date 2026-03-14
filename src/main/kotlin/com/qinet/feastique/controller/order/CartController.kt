package com.qinet.feastique.controller.order

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.order.CartItemDto
import com.qinet.feastique.model.dto.order.ChangeQuantityDto
import com.qinet.feastique.model.dto.order.ItemDto
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
@RequestMapping("/api/v1/customers/{customerId}/cart")
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
        @RequestBody itemDto: ItemDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<CartResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val cart = cartService.addItemToCart(itemDto, customerDetails)
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

    @PatchMapping("/quantity/{id}")
    fun changeQuantity(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID,
        @RequestBody @Valid changeQuantityDto: ChangeQuantityDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        cartService.changeQuantity(id, customerDetails, changeQuantityDto)

        return if (changeQuantityDto.quantity != 0) {
            ResponseEntity("Item quantity changed to ${changeQuantityDto.quantity}.", HttpStatus.OK)
        } else {
            ResponseEntity("Item has been removed from the cart.", HttpStatus.OK)
        }
    }
}

