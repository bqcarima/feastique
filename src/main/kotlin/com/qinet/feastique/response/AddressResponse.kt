package com.qinet.feastique.response

data class AddressResponse(
    val id: Long,
    val country: String,
    val region: String,
    val city: String,
    val neighbourhood: String,
    val streetName: String?,
    val directions: String,
    val location: List<String>?
)