package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.order.FoodOrderDto
import com.qinet.feastique.response.order.FoodOrderResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.order.FoodOrderService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class OrderController(
    private val foodOrderService: FoodOrderService,
    private val securityUtility: SecurityUtility
) {
    @PutMapping("/customers/{customerId}/orders")
    fun placeOrder(
        @PathVariable customerId:Long,
        @RequestBody @Valid foodOrderDto: FoodOrderDto,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<FoodOrderResponse> {
        securityUtility.validatePath(customerId, customerDetails)
        val foodOrder = foodOrderService.placeFoodOrder(foodOrderDto, customerDetails)
        return ResponseEntity(foodOrder.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/customers/{customerId}/orders/cancel/{id}")
    fun cancelOrder(
        @PathVariable id: Long,
        @PathVariable customerId: Long,
        @AuthenticationPrincipal customerDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(customerId, customerDetails)
        foodOrderService.cancelOrder(id, customerDetails)
        return ResponseEntity("Order cancelled successfully.", HttpStatus.OK)
    }

    @PutMapping(
        path = [
            "/customers/{customerId}/orders/delete/{id}",
            "/vendors/{vendorId}/orders/delete/{id}"
        ]
    )
    fun deleteOrder(
        @PathVariable id: Long,
        @PathVariable(required = false) customerId: Long?,
        @PathVariable(required = false) vendorId: Long?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<String> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        foodOrderService.deleteOrder(id, userDetails)
        return ResponseEntity("Order deleted successfully.", HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/orders/{id}",
            "/vendors/{vendorId}/orders/{id}"
        ]
    )
    fun getOrder(
        @PathVariable id: Long,
        @PathVariable(required = false) customerId: Long?,
        @PathVariable(required = false) vendorId: Long?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<FoodOrderResponse> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val order = foodOrderService.getOrder(pathId, userDetails)
        return ResponseEntity(order?.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/orders",
            "/vendors/{vendorId}/orders"
        ]
    )
    fun getAllOrders(
        @PathVariable(required = false) customerId: Long?,
        @PathVariable(required = false) vendorId: Long?,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<List<FoodOrderResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId!!, userDetails)
        val orders = foodOrderService.getAllOrders(userDetails)
        return ResponseEntity(orders.map {it.toResponse()}, HttpStatus.OK)
    }
}