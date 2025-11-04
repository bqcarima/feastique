package com.qinet.feastique.repository.sales

import com.qinet.feastique.model.entity.sales.ComplementSale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ComplementSaleRepository : JpaRepository<ComplementSale, UUID>