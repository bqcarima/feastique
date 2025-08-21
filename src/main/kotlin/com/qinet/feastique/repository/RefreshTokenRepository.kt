package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findByCustomerId(customerId: Long): RefreshToken?
    fun deleteByCustomerId(customerId: Long)
    fun findByHashedToken(hashedToken: String): RefreshToken?

    fun findByVendorId(vendorId: Long): RefreshToken?
    fun deleteByVendorId(vendorId: Long)
}