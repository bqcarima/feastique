package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.beverage.Beverage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BeverageRepository : JpaRepository<Beverage, Long> {

    fun findAllByVendorId(vendorId: Long): List<Beverage>
    fun findAllByIdInAndVendorId(beverageIds: List<Long>, vendorId: Long): List<Beverage>
    fun findFirstByBeverageNameIgnoreCaseAndVendorId(name: String, vendorId: Long): Beverage?
}