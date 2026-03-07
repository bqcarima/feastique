package com.qinet.feastique.response.user

import java.util.*

data class AddressResponse(
    val id: UUID,
    val country: String,
    val region: String,
    val city: String,
    val neighbourhood: String,
    val streetName: String?,
    val directions: String,
    val location: List<String>?
)

data class CustomerAddressResponse(
    val id: UUID,
    val country: String,
    val region: String,
    val city: String,
    val neighbourhood: String,
    val streetName: String?,
    val directions: String,
    val location: List<String>?,
    val default: Boolean
)

