package com.qinet.feastique.response

import java.util.UUID

data class ComplementResponse(
    val id: UUID,
    val name: String,
    val price: Long
)