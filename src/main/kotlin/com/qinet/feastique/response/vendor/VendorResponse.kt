package com.qinet.feastique.response.vendor

import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.response.address.AddressResponse
import com.qinet.feastique.response.ComplementResponse
import com.qinet.feastique.response.DiscountResponse
import com.qinet.feastique.response.PhoneNumberResponse
import com.qinet.feastique.response.food.FoodResponse
import java.util.Date

data class VendorResponse(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val chefName: String,
    val restaurantName: String,
    val balance: Long,
    val verified: Boolean,
    val accountType: String,
    val imageUrl: String,
    val registrationDate: Date,
    val phoneNumber: List<PhoneNumberResponse>,
    val address: AddressResponse,
    val food: List<FoodResponse>,
    val addOn: List<AddOnResponse>,
    val complement: List<ComplementResponse>,
    val discount: List<DiscountResponse>,
    )