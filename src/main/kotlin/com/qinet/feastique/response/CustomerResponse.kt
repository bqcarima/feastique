package com.qinet.feastique.response

import com.qinet.feastique.response.address.CustomerAddressResponse
import java.time.LocalDate
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

