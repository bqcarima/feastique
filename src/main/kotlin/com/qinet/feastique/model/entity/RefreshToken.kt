package com.qinet.feastique.model.entity

import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID = UuidCreator.getTimeOrdered(),
    val customerId: UUID?,
    val vendorId: UUID?,
    val expiresAt: Date,
    val hashedToken: String,
    val createdAt: Instant = Instant.now()
)
