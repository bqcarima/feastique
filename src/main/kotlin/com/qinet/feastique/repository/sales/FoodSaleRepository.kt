package com.qinet.feastique.repository.sales

import com.qinet.feastique.model.entity.sales.FoodSale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodSaleRepository : JpaRepository<FoodSale, UUID>