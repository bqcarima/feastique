package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.FoodOrderType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderTypeRepository : JpaRepository<FoodOrderType, Long>