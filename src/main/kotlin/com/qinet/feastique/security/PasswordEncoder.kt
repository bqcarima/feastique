package com.qinet.feastique.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    fun encode(raw: String): String = bcrypt.encode(raw)

    fun matches(raw: String, hash: String): Boolean = bcrypt.matches(raw, hash)
}