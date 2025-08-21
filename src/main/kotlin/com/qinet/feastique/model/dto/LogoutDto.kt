package com.qinet.feastique.model.dto

data class LogoutDto(
    val accessToken: String,
    val refreshToken: String
)
