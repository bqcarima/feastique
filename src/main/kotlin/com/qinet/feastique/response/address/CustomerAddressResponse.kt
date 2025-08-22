package com.qinet.feastique.response.address

data class CustomerAddressResponse(
    val id: Long,
    val country: String,
    val region: String,
    val city: String,
    val neighbourhood: String,
    val streetName: String?,
    val directions: String,
    val location: List<String>?,
    val default: Boolean
)
