package com.qinet.feastique.response.image

import java.util.UUID

data class ImageResponse(
    val id: UUID,
    val imageUrl: String,
)