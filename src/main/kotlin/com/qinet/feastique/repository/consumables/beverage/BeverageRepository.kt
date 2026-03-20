package com.qinet.feastique.repository.consumables.beverage

import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BeverageRepository : JpaRepository<Beverage, UUID> {

    fun findByIdAndIsActiveTrue(id: UUID): Beverage?
    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, pageable: Pageable): Page<Beverage>
    fun findByIdAndVendorIdAndIsActiveTrue(id: UUID, vendorId: UUID): Beverage?

    fun findAllByVendorIdAndIsActiveTrue(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Beverage>

    fun existsByNameIgnoreCaseAndVendorIdAndIsActiveTrue(name: String, vendorId: UUID): Boolean
}

