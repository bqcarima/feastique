package com.qinet.feastique.repository.complement

import com.qinet.feastique.model.entity.complement.Complement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ComplementRepository : JpaRepository<Complement, Long> {

    fun findAllByVendorId(vendorId: Long): List<Complement>
    fun findAllByIdInAndVendorId(complementIds: List<Long>, vendorId: Long): List<Complement>
    fun findFirstByComplementNameIgnoreCaseAndVendorId(complementName: String, vendorId: Long): Complement?
    fun existsByComplementNameIgnoreCaseAndVendorId(complementName: String, vendorId: Long): Boolean
}

