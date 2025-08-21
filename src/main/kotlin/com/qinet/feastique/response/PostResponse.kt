package com.qinet.feastique.response

import java.util.Date

data class PostResponse(
    val id: Long,
    val title: String,
    val body: String,
    val image: String,
    val likes: Long,
    val postDate: Date
)
