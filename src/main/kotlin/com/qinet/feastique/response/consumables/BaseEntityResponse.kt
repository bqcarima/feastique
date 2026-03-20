package com.qinet.feastique.response.consumables

import java.util.UUID

interface BaseEntityResponse {
    val id: UUID
    val likeCount: Long
    val likedByCurrentUser: Boolean
    val bookmarkCount: Long
    val bookmarkedByCurrentUser: Boolean
}

