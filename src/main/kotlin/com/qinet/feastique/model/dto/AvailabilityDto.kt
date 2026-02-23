package com.qinet.feastique.model.dto

import java.util.UUID

data class AvailabilityDto(
    val id: UUID? = null,
    val availableDay: String
)

