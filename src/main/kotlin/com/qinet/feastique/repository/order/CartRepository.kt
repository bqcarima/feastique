package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByCustomerId(customerId: UUID): Optional<Cart>
}

