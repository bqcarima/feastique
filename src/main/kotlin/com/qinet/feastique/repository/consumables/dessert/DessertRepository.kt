package com.qinet.feastique.repository.consumables.dessert

import com.qinet.feastique.model.entity.consumables.dessert.Dessert
import com.qinet.feastique.model.enums.DessertType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DessertRepository : JpaRepository<Dessert, UUID> {
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Dessert>
    fun findAllByIdInAndVendorId(ids: List<UUID>, vendorId: UUID, pageable: Pageable): Page<Dessert>

    fun findAllByDessertTypeInAndVendorId(dessertTypes: List<DessertType>, vendorId: UUID, pageable: Pageable): Page<Dessert>
    fun existsByNameIgnoreCaseAndVendorId(name: String, vendorId: UUID): Boolean
}

