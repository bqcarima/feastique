package com.qinet.feastique.repository.sales

import com.qinet.feastique.model.entity.sales.AddOnSale
import com.qinet.feastique.model.entity.sales.BeverageSale
import com.qinet.feastique.model.entity.sales.ComplementSale
import com.qinet.feastique.model.entity.sales.FoodSale
import com.qinet.feastique.model.entity.sales.DessertSale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AddOnSaleRepository : JpaRepository<AddOnSale, UUID>

@Repository
interface BeverageSaleRepository : JpaRepository<BeverageSale, UUID>

@Repository
interface ComplementSaleRepository : JpaRepository<ComplementSale, UUID>

@Repository
interface FoodSaleRepository : JpaRepository<FoodSale, UUID>

@Repository
interface DessertSaleRepository : JpaRepository<DessertSale, UUID>