package com.qinet.feastique.response

import java.util.UUID

data class AddOnResponse(
    val id: UUID,
    val addOnName: String,
    val price: Long,
    )

