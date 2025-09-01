package com.qinet.feastique.response.order

import com.qinet.feastique.model.enums.OrderType
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.BeverageResponse
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.address.AddressResponse
import com.qinet.feastique.response.address.CustomerAddressResponse
import com.qinet.feastique.response.food.FoodMinimalResponse
import com.qinet.feastique.response.food.FoodSizeResponse
import java.time.LocalDateTime
import java.time.LocalTime

data class FoodOrderResponse(
    val id: Long,
    val customer: FoodOrderCustomerResponse,
    val vendor: FoodOrderVendorResponse,
    val food: FoodMinimalResponse,
    val complement: ComplementResponse,
    val size: FoodSizeResponse,
    val orderAddOn: List<AddOnResponse>,
    val orderBeverage: List<BeverageResponse>,
    val customerAddress: CustomerAddressResponse,
    val vendorAddress: AddressResponse,
    val placementTime: LocalDateTime?,
    val responseTime: LocalDateTime?,
    val deliveryTime: LocalTime?,
    val totalAmount: Long,
    val orderType: OrderType?,
    val completedTime: LocalDateTime?,
    val customerDeletedStatus: Boolean?,
    val vendorDeletedStatus: Boolean?
)
