package com.qinet.feastique.response.post

import java.util.Date
import java.util.UUID

data class PostResponse(
    val id: UUID,
    val title: String,
    val body: String,
    val image: String,
    val likes: Long,
    val postDate: Date
)