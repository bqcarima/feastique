package com.qinet.feastique.response

import java.util.UUID

data class PhoneNumberResponse(
    val id: UUID,
    val phoneNumber: String,
    val default: Boolean,
)
