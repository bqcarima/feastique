package com.qinet.feastique.response

data class BeverageResponse(
    val id: Long,
    val beverageName: String,
    val alcoholic: Boolean,
    val percentage: Int,
    val beverageGroup: String,
    val price: Long,
    val delivery: Boolean,
)
