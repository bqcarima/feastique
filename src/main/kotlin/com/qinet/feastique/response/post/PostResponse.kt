package com.qinet.feastique.response.post

import com.qinet.feastique.response.image.ImageResponse
import java.util.*

data class PostResponse(
    val id: UUID,
    val title: String,
    val body: String?,
    val images: Set<ImageResponse>,
    val likeCount: Long,
    val likedByCurrentUser: Boolean,
    val createdAt: String,
    val updatedAt: String?
)

