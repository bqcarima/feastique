package com.qinet.feastique.response.user

import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.consumables.food.AddOnResponse
import com.qinet.feastique.response.consumables.food.ComplementResponse
import com.qinet.feastique.response.consumables.food.FoodResponse
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class CustomerResponse(
    val id: UUID,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: List<PhoneNumberResponse>,
    val address: List<CustomerAddressResponse>,
    val dob: LocalDate?,
    val anniversary: LocalDate?,
    val verified: Boolean,
    val accountType: String,
    val imageUrl: String,
    val registrationDate: Date,
)


data class VendorResponse(
    val id: UUID,
    val vendorCode: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val chefName: String,
    val restaurantName: String,
    val balance: Long,
    val openingTime: LocalTime?,
    val closingTime: LocalTime?,
    val verified: Boolean,
    val accountType: String?,
    val imageUrl: String,
    val registrationDate: Date,
    val phoneNumber: List<PhoneNumberResponse>,
    val address: AddressResponse,
    val food: List<FoodResponse>,
    val addOn: List<AddOnResponse>,
    val complement: List<ComplementResponse>,
    val discount: List<DiscountResponse>,
)

data class VendorMinimalResponse(
    val id: UUID,
    val username: String,
    val vendorCode: String,
    val firstName: String,
    val lastName: String,
    val chefName: String,
    val restaurantName: String,
    val balance: Long,
    val verified: Boolean,
    val phoneNumber: List<PhoneNumberResponse>,
    val address: AddressResponse,
    val registrationDate: Date,
    val openingTime: LocalTime?,
    val closingTime: LocalTime?
)

