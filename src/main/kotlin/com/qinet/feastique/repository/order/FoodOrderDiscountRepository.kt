package com.qinet.feastique.repository.order

import com.qinet.feastique.model.entity.discount.OrderDiscount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderDiscountRepository : JpaRepository<OrderDiscount, Long>