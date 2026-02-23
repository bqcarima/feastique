package com.qinet.feastique.model.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class ImageDto(
    var id: UUID? = null,

    @field:NotNull(message = "Image Url cannot be empty.")
    var imageUrl: String?
)

