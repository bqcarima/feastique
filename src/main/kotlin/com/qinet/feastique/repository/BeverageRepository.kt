package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.beverage.Beverage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BeverageRepository : JpaRepository<Beverage, UUID> {

    fun findAllByVendorId(vendorId: UUID): List<Beverage>
    fun findAllByIdInAndVendorId(beverageIds: List<UUID>, vendorId: UUID): List<Beverage>
    fun findFirstByBeverageNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Beverage?
    fun existsByBeverageNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}