package com.qinet.feastique.service

import com.qinet.feastique.model.entity.RefreshToken
import com.qinet.feastique.repository.RefreshTokenRepository
import com.qinet.feastique.security.HashEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hashEncoder: HashEncoder
) {

    @Transactional
    fun storeRefreshToken(refreshToken: RefreshToken) {
        refreshTokenRepository.save(refreshToken)
    }

    @Transactional(readOnly = true)
    fun getTokenByCustomerId(customerId: UUID): RefreshToken? {
        return refreshTokenRepository.findByCustomerId(customerId)
    }

    @Transactional
    fun deleteTokenByCustomerId(customerId: UUID) {
        refreshTokenRepository.deleteByCustomerId(customerId)
    }

    @Transactional(readOnly = true)
    fun getTokenByVendorId(vendorId: UUID): RefreshToken? {
        return refreshTokenRepository.findByVendorId(vendorId)
    }

    @Transactional
    fun deleteToken(refreshToken: RefreshToken) {
        refreshTokenRepository.delete(refreshToken)
    }
    @Transactional
    fun deleteTokenByVendorId(vendorId: UUID) {
        refreshTokenRepository.deleteByVendorId(vendorId)
    }

    fun revokeByRefreshToken(rawRefreshToken: String) {
        val hashedToken = hashEncoder.encode(rawRefreshToken)
        val token = refreshTokenRepository.findByHashedToken(hashedToken)
            ?: return // no token found, nothing to revoke

        refreshTokenRepository.delete(token)
    }
}

