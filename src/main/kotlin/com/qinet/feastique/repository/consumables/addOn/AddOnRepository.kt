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
    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, pageable: Pageable): Page<AddOn>
    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<AddOn>
    fun existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(addOnName: String, vendorId: UUID): Boolean
}

