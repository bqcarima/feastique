package com.qinet.feastique.response.order

import com.qinet.feastique.model.enums.OrderStatus
import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.response.address.CustomerAddressResponse
import com.qinet.feastique.response.beverage.BeverageItemResponse
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val userOrderCode: String,
    val customer: FoodOrderCustomerResponse,
    val customerAddress: CustomerAddressResponse?,
    val vendor: FoodOrderVendorResponse,
    val foodOrderItems: List<FoodOrderItemResponse>,
    val beverageOrderItems: List<BeverageItemResponse>,
    val orderType: OrderType?,
    val deliveryFee: Long?,
    val orderStatus: OrderStatus?,
    val totalAmount: Long,
    val placementTime: LocalDateTime?,
    val responseTime: LocalDateTime?,
    val deliveryTime: LocalTime?,
    val completedTime: LocalDateTime?
)

