package com.qinet.feastique.repository.discount

import com.qinet.feastique.model.entity.discount.FoodDiscount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodDiscountRepository : JpaRepository<FoodDiscount, UUID>

