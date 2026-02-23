package com.qinet.feastique.response.user

import java.util.UUID

data class PhoneNumberResponse(
    val id: UUID,
    val phoneNumber: String,
    val default: Boolean,
)