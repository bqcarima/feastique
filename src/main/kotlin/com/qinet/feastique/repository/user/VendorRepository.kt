package com.qinet.feastique.repository.user

import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.Region
import jakarta.persistence.LockModeType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VendorRepository : JpaRepository<Vendor, UUID> {

    // fun findAll(scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Vendor>
    fun findFirstByUsername(username: String): Vendor?
    fun existsByUsernameIgnoreCase(username: String): Boolean

    @EntityGraph("Vendor.withAddressAndPhoneNumber")
    @Query("SELECT v FROM Vendor v WHERE v.id = :id")
    fun findVendorByIdWithAddressAndPhoneNumber(id: UUID): Vendor?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vendor v WHERE v.region = :region ORDER BY v.vendorCode DESC")
    fun findTopByRegionOrderByVendorCodeDescWithLock(region: Region, pageable: Pageable = PageRequest.of(0, 1)): List<Vendor>

}

