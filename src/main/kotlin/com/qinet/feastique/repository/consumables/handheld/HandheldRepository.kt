package com.qinet.feastique.repository.consumables.handheld

import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HandheldRepository : JpaRepository<Handheld, UUID> {

    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Handheld>
    fun findAllByIdInAndVendorId(handheldIds: List<UUID>, vendorId: UUID): List<Handheld>
    fun findFirstByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Handheld?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Handheld h ORDER BY h.handheldNumber desc ")
    fun findTopOrderByFoodNumberDescWithLock(pageable: Pageable = PageRequest.of(0, 1)): List<Handheld>
    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}
