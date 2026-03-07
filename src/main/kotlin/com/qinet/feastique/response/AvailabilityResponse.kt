package com.qinet.feastique.response

import java.util.UUID

data class AvailabilityResponse(
    val id: UUID,
    val availability: String,
)