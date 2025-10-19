package com.qinet.feastique.response.address

import java.util.UUID

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