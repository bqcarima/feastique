package com.qinet.feastique.repository.consumables.addOn

import com.qinet.feastique.model.entity.consumables.addOn.AddOn
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
interface AddOnRepository : JpaRepository<AddOn, UUID> {
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<AddOn>
    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<AddOn>
    fun findAllByIdInAndVendorId(addOnIds: List<UUID>, vendorId: UUID, pageable: Pageable): Page<AddOn>
    fun existsByNameIgnoreCaseAndVendorId(addOnName: String, vendorId: UUID): Boolean
}

