package com.qinet.feastique.repository.authentication

import com.qinet.feastique.model.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    fun findByCustomerId(customerId: UUID): RefreshToken?
    fun deleteByCustomerId(customerId: UUID)
    fun findByHashedToken(hashedToken: String): RefreshToken?

    fun findByVendorId(vendorId: UUID): RefreshToken?
    fun deleteByVendorId(vendorId: UUID)
}