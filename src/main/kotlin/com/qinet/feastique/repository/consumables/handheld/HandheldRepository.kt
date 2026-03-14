package com.qinet.feastique.repository.consumables.handheld

import com.qinet.feastique.model.entity.consumables.handheld.Handheld
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HandheldRepository : JpaRepository<Handheld, UUID> {

    // Pageable with Page<Handheld>
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Handheld>

    // Windows-based scroll
    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Handheld>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Handheld h ORDER BY h.handheldNumber desc ")
    fun findTopOrderByFoodNumberDescWithLock(pageable: Pageable = PageRequest.of(0, 1)): List<Handheld>
    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

