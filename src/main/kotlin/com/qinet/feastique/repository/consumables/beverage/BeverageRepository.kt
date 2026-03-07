package com.qinet.feastique.repository.consumables.beverage

import com.qinet.feastique.model.entity.consumables.beverage.Beverage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BeverageRepository : JpaRepository<Beverage, UUID> {

    @Query("SELECT b FROM Beverage b WHERE b.vendor.id = :vendorId ORDER BY b.createdAt ASC")
    fun findAllOrdered(@Param("vendorId") vendorId: UUID, pageable: Pageable): Page<Beverage>

    fun findAllByAlcoholic(alcoholic: Boolean, window: Window<Beverage>): Slice<Beverage>
    fun findAllByIdInAndVendorId(beverageIds: List<UUID>, vendorId: UUID, window: Window<Beverage>): Slice<Beverage>
    fun findFirstByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Beverage?
    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

