package com.qinet.feastique.response.vendor

import com.qinet.feastique.response.address.AddressResponse
import com.qinet.feastique.response.PhoneNumberResponse
import java.util.Date

data class VendorMinimalResponse(
    val username: String,
    val firstName: String,
    val lastName: String,
    val chefName: String,
    val restaurantName: String,
    val balance: Long,
    val verified: Boolean,
    val phoneNumber: List<PhoneNumberResponse>,
    val address: AddressResponse,
    val registrationDate: Date,
)