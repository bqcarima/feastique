package com.qinet.feastique.repository.sales

import com.qinet.feastique.model.entity.sales.AddOnSale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AddOnSaleRepository : JpaRepository<AddOnSale, UUID>