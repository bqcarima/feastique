package com.qinet.feastique.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.Date

/**
 * Persisted server-side session that represents a single valid access token.
 *
 * Storing sessions like this makes logout trivial: delete the row with the
 * corresponding [tokenIdentifier] and the JWT is immediately invalid for future requests.
 *
 * @property id database primary key
 * @property tokenIdentifier unique identifier stored inside the JWT (claim "tokenIdentifier")
 * @property userId the id of the user (customer or vendor) this session belongs to
 * @property userType string describing the account type (e.g. "CUSTOMER" or "VENDOR")
 * @property expiresAtEpochMillis epoch milliseconds when the access token naturally expires
 * @property createdAt timestamp when the session was created
 */
@Entity
@Table(
    name = "user_session",
    indexes = [
        Index(name = "idx_user_session_token_identifier", columnList = "token_identifier"),
        Index(name = "idx_user_session_user_id", columnList = "user_id")
    ]
)
data class UserSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "token_identifier", nullable = false, unique = true, length = 128)
    val tokenIdentifier: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "user_type", nullable = false, length = 50)
    val userType: String,

    /**
     * Expiration stored as epoch milliseconds to make comparisons cheap and avoid timezone issues.
     */
    @Column(name = "expires_at_epoch_millis", nullable = false)
    val expiresAtEpochMillis: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Date = Date()
) {
    /**
     * Convenience helper to check whether this session has expired.
     */
    fun isExpired(currentEpochMillis: Long = System.currentTimeMillis()): Boolean =
        expiresAtEpochMillis <= currentEpochMillis
}