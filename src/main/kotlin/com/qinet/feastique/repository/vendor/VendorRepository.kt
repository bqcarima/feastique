package com.qinet.feastique.repository.vendor

import com.qinet.feastique.model.entity.user.Vendor
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VendorRepository : JpaRepository<Vendor, UUID> {
    fun findFirstByUsername(username: String): Vendor?
    fun existsByUsernameIgnoreCase(username: String): Boolean

    @EntityGraph("Vendor.withAddressAndPhoneNumber")
    @Query("SELECT v FROM Vendor v WHERE v.id = :id")
    fun findVendorByIdWithAddressAndPhoneNumber(id: UUID): Vendor?

    @EntityGraph("Vendor.withFoodAndDiscounts")
    @Query("SELECT v FROM Vendor v WHERE v.id = :id")
    fun findByIdAndLoadFoodAndDiscounts(id: UUID): Vendor?
    @EntityGraph("Vendor.withAllRelations")
    @Query("SELECT v FROM Vendor v WHERE v.id = :id")
    fun findByIdWithAllRelations(id: UUID): Vendor?
}

