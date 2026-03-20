package com.qinet.feastique.response.bookmark

import com.qinet.feastique.response.consumables.BaseEntityResponse
import java.util.*

data class BookmarkResponse(
    val id: UUID,
    val item: BaseEntityResponse,
    val createdAt: String
)

