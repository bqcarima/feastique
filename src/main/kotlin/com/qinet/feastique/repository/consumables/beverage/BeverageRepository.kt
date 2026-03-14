package com.qinet.feastique.repository.consumables.beverage

import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BeverageRepository : JpaRepository<Beverage, UUID> {

    @Query("SELECT b FROM Beverage b WHERE b.vendor.id = :vendorId ORDER BY b.createdAt ASC")
    fun findAllOrdered(@Param("vendorId") vendorId: UUID, pageable: Pageable): Page<Beverage>

    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Beverage>

    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

