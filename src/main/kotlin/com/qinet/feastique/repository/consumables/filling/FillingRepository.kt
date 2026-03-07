package com.qinet.feastique.repository.consumables.filling

import com.qinet.feastique.model.entity.consumables.filling.Filling
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FillingRepository : JpaRepository<Filling, UUID> {

    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Filling>
    fun findAllByIdInAndVendorId(fillingIds: List<UUID>, vendorId: UUID, pageable: Pageable): Page<Filling>
    fun existsByNameIgnoreCaseAndVendorId(fillingName: String, vendorId: UUID): Boolean
}