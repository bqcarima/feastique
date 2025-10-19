package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.UserSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserSessionRepository : JpaRepository<UserSession, UUID> {
    fun findByTokenIdentifier(tokenIdentifier: String): UserSession?
    fun existsByTokenIdentifier(tokenIdentifier: String): Boolean
    fun deleteByTokenIdentifier(tokenIdentifier: String)
    fun deleteByUserIdAndUserType(userId: UUID, userType: String)
    fun deleteByExpiresAtEpochMillisLessThan(expiryThresholdEpochMillis: Long)
}