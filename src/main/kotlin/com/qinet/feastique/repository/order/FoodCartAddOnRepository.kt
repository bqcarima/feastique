package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.beverage.OrderBeverage
import org.springframework.data.jpa.repository.JpaRepository

interface FoodCartAddOnRepository : JpaRepository<OrderBeverage, Long> {
}