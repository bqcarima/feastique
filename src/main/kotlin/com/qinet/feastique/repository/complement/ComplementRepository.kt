package com.qinet.feastique.repository.complement

import com.qinet.feastique.model.entity.complement.Complement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ComplementRepository : JpaRepository<Complement, UUID> {

    fun findAllByVendorId(vendorId: UUID): List<Complement>
    fun findAllByIdInAndVendorId(complementIds: List<UUID>, vendorId: UUID): List<Complement>
    fun findFirstByComplementNameIgnoreCaseAndVendorId(complementName: String, vendorId: UUID): Complement?
    fun existsByComplementNameIgnoreCaseAndVendorId(complementName: String, vendorId: UUID): Boolean
}

