package com.qinet.feastique.response.token

data class AccessTokenResponse(
    val accessToken: String,
)

data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String
)
