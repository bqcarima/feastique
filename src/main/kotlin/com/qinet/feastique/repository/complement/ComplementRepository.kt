package com.qinet.feastique.repository.complement

import com.qinet.feastique.model.entity.complement.Complement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ComplementRepository : JpaRepository<Complement, Long> {

    fun findAllByVendorId(vendorId: Long): List<Complement>
    fun deleteByIdAndVendorId(id: Long, vendorId: Long)
    fun findByIdAndVendorId(complementId: Long, vendorId: Long): Complement?
    fun findAllByIdInAndVendorId(complementIds: List<Long>, vendorId: Long): List<Complement>
    fun findByComplementNameIgnoreCaseAndVendorId(complementName: String, vendorId: Long): Complement?
}

