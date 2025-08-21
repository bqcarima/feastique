package com.qinet.feastique.security

import com.qinet.feastique.service.RefreshTokenService
import com.qinet.feastique.service.UserSessionService
import org.springframework.stereotype.Service

@Service
class SessionManager(
    private val refreshTokenService: RefreshTokenService,
    private val userSessionService: UserSessionService,
) {

    fun resetSessions(userId: Long, userType: String) {

        // Remove refresh tokens
        when(userType) {
            "CUSTOMER" -> refreshTokenService.deleteTokenByCustomerId(userId)
            "VENDOR" -> refreshTokenService.deleteTokenByVendorId(userId)
        }
        // Clear all sessions
        userSessionService.deleteAllSessionsForUser(userId, userType)
    }
}