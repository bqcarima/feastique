package com.qinet.feastique.security

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64


@Component
class HashEncoder {

    fun encode(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun matches(raw: String, hashed: String): Boolean {
        return encode(raw) == hashed
    }
}