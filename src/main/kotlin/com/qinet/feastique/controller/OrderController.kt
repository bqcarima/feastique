package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.order.FoodOrderDto
import com.qinet.feastique.model.dto.order.FoodOrderUpdateDto
import com.qinet.feastique.response.order.OrderResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.order.OrderService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class OrderController(
    private val orderService: OrderService,
    private val securityUtility: SecurityUtility
) {
    @PutMapping("/customers/{customerId}/orders")
    fun placeOrder(
        @PathVariable customerId: UUID,
        @RequestBody @Valid foodOrderDto: FoodOrderDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<OrderResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val order = orderService.placeFoodOrder(foodOrderDto, customerDetails)
        return ResponseEntity(order.toResponse(), HttpStatus.CREATED)
    }

    @PutMapping(
        path = [
            "/customers/{customerId}/orders/cancel/{id}",
            "/vendors/{vendorId}/orders/update/{id}"
        ]
    )
    fun cancelOrUpdateOrder(
        @PathVariable id: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @RequestBody @Valid foodOrderUpdateDto: FoodOrderUpdateDto,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<String> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        orderService.cancelOrUpdateOrder(id, foodOrderUpdateDto, userDetails)
        return ResponseEntity("Order updated successfully.", HttpStatus.OK)
    }

    @PutMapping(
        path = [
            "/customers/{customerId}/orders/delete/{id}",
            "/vendors/{vendorId}/orders/delete/{id}"
        ]
    )
    fun deleteOrder(
        @PathVariable id: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<String> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        orderService.deleteOrder(id, userDetails)
        return ResponseEntity("Order deleted successfully.", HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/orders/{id}",
            "/vendors/{vendorId}/orders/{id}"
        ]
    )
    fun getOrder(
        @PathVariable id: UUID,
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<OrderResponse> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val order = orderService.getOrder(pathId, userDetails)
        return ResponseEntity(order?.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/orders",
            "/vendors/{vendorId}/orders"
        ]
    )
    fun getAllOrders(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable(required = false) vendorId: UUID?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<List<OrderResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val orders = orderService.getAllOrders(userDetails)
        return ResponseEntity(orders.map {it.toResponse()}, HttpStatus.OK)
    }
}

