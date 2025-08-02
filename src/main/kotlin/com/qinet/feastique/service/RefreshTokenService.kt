package com.qinet.feastique.service

import com.qinet.feastique.model.entity.RefreshToken
import com.qinet.feastique.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun storeRefreshToken(refreshToken: RefreshToken) {
        refreshTokenRepository.save(refreshToken)
    }

    fun getTokenByCustomerId(customerId: Long): RefreshToken? {
        return refreshTokenRepository.findByCustomerId(customerId)
    }

    fun deleteTokenByCustomerId(customerId: Long) {
        refreshTokenRepository.deleteByCustomerId(customerId)
    }

    fun getTokenByVendorId(vendorId: Long): RefreshToken? {
        return refreshTokenRepository.findByVendorId(vendorId)
    }

    fun deleteTokenByVendorId(vendorId: Long) {
        refreshTokenRepository.deleteByVendorId(vendorId)
    }
}