package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.beverage.OrderBeverage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodCartBeverageRepository : JpaRepository<OrderBeverage, Long> {
}