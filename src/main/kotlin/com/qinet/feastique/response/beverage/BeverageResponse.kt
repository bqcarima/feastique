package com.qinet.feastique.response.beverage

import java.util.UUID

data class BeverageResponse(
    val id: UUID,
    val beverageName: String,
    val alcoholic: Boolean,
    val percentage: Int,
    val beverageGroup: String,
    val price: Long,
    val delivery: Boolean,
)