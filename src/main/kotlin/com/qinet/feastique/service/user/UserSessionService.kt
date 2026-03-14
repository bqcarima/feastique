package com.qinet.feastique.service.user

import com.qinet.feastique.model.entity.UserSession
import com.qinet.feastique.repository.authentication.UserSessionRepository
import com.qinet.feastique.service.authentication.RefreshTokenService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.util.UUID

@Service
class UserSessionService(
    private val userSessionRepository: UserSessionRepository,
    private val refreshTokenService: RefreshTokenService,

    ) {
    fun createSession(
        tokenIdentifier: String,
        userId: UUID,
        userType: String,
        expiresAtEpocMillis: Long
    ): UserSession {

        /**
         * Persist a new session representing a valid access token.
         * If a session with the same tokenIdentifier already exists it will be overwritten by save().
         */
        val session = UserSession(
            tokenIdentifier = tokenIdentifier,
            userId = userId,
            userType = userType,
            expiresAtEpochMillis = expiresAtEpocMillis,
            createdAt = Date()
        )

        return userSessionRepository.save(session)
    }

    /**
     * Return the session for the given tokenIdentifier or null if not found.
     */
    @Transactional(readOnly = true)
    fun getSession(tokenIdentifier: String): UserSession? {
        return userSessionRepository.findByTokenIdentifier(tokenIdentifier)
    }

    /**
     * Delete a session by its token identifier. Idempotent.
     */
    @Transactional
    fun deleteSession(tokenIdentifier: String) {
        if (userSessionRepository.existsByTokenIdentifier(tokenIdentifier)) {
            userSessionRepository.deleteByTokenIdentifier(tokenIdentifier)
        }
    }

    fun deleteAllSessionsForUser(userId: UUID, userType: String ) {
        userSessionRepository.deleteByUserIdAndUserType(userId, userType)
    }

    @Transactional
    fun resetSessions(userId: UUID, userType: String) {

        // Remove refresh tokens
        when(userType) {
            "CUSTOMER" -> refreshTokenService.deleteTokenByCustomerId(userId)
            "VENDOR" -> refreshTokenService.deleteTokenByVendorId(userId)
        }
        // Clear all sessions
        deleteAllSessionsForUser(userId, userType)
    }

    /**
     * Cleanup expired sessions. This method deletes sessions whose expiry is before the provided threshold.
     * By default, it removes sessions already expired at the moment of invocation.
     *
     * Optional: annotate a scheduled cron or fixedDelay to run periodically.
     */
    @Transactional
    fun cleanupExpiredSessions(expiryThresholdEpochMillis: Long) {
        userSessionRepository.deleteByExpiresAtEpochMillisLessThan(expiryThresholdEpochMillis)
    }

    /**
     * Optional scheduled housekeeping. Enable scheduling in your application by adding
     * `@EnableScheduling` to a configuration class.
     *
     * Runs every 6 hours by default; adjust cron or fixedDelay to taste.
     *
     * If you don't want scheduled cleanup, just remove this method.
     */
    @Scheduled(fixedDelayString = $$"${feastique.session.cleanup.fixedDelayMillis:21600000}")
    fun scheduledCleanupExpiredSessions() {
        cleanupExpiredSessions(System.currentTimeMillis())
    }
}