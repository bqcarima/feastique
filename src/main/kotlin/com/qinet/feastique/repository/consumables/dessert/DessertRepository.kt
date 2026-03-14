package com.qinet.feastique.repository.consumables.dessert

import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DessertRepository : JpaRepository<Dessert, UUID> {
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Dessert>

    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Dessert>

    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

