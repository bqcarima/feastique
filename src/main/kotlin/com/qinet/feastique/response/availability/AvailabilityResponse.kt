package com.qinet.feastique.response.availability

import java.util.UUID

data class AvailabilityResponse(
    val id: UUID,
    val availability: String,
)