package com.qinet.feastique.repository.consumables.complement

import com.qinet.feastique.model.entity.consumables.complement.Complement
import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ComplementRepository : JpaRepository<Complement, UUID> {

    fun findByIdAndVendorIdAndIsActiveTrue(complementId: UUID, vendorId: UUID): Complement?
    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, pageable: Pageable): Page<Complement>
    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Complement>
    fun existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(complementName: String, vendorId: UUID): Boolean
}

