package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.order.FoodCart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodCartRepository : JpaRepository<FoodCart, Long>