package com.qinet.feastique.response.pagination

data class WindowResponse<T>(
    val content: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean
)

