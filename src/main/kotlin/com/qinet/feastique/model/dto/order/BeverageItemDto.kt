package com.qinet.feastique.model.dto.order

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class BeverageItemDto(

    @field:NotNull(message = "Beverage Id cannot be empty.")
    var beverageIds: Map<UUID, Int>?
)

