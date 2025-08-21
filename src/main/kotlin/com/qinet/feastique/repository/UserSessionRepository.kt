package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.UserSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSessionRepository : JpaRepository<UserSession, Long> {
    fun findByTokenIdentifier(tokenIdentifier: String): UserSession?
    fun existsByTokenIdentifier(tokenIdentifier: String): Boolean
    fun deleteByTokenIdentifier(tokenIdentifier: String)
    fun deleteByUserIdAndUserType(userId: Long, userType: String)
    fun deleteByExpiresAtEpochMillisLessThan(expiryThresholdEpochMillis: Long)
}