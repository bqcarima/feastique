package com.qinet.feastique.response.order

import com.qinet.feastique.response.user.CustomerAddressResponse
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

data class OrderResponse(
    val id: UUID,
    val userOrderCode: String,
    val customer: FoodOrderCustomerResponse,
    val customerAddress: CustomerAddressResponse?,
    val vendor: FoodOrderVendorResponse,
    val items: List<BaseResponseEntity>,
    val orderType: String?,
    val deliveryFee: Long?,
    val orderStatus: String?,
    val totalAmount: Long,
    val placementTime: LocalDateTime?,
    val responseTime: LocalDateTime?,
    val deliveryTime: LocalTime?,
    val completedTime: LocalDateTime?
)

data class OrderReviewResponse(
    val orderId: UUID,
    val orderType: String?
)

