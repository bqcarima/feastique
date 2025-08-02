package com.qinet.feastique.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "refresh_token")
data class RefreshToken(
    @Id
    @GeneratedValue
    val id: Long? = null,
    val customerId: Long?,
    val vendorId: Long?,
    val expiresAt: Date,
    val hashedToken: String,
    val createdAt: Instant = Instant.now()
)
