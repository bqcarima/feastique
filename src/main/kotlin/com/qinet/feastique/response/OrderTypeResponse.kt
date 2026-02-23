package com.qinet.feastique.response

import java.util.UUID

data class OrderTypeResponse(
    val id: UUID,
    val orderType: String,
)