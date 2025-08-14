package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.food.FoodAvailability
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodAvailabilityRepository : JpaRepository<FoodAvailability, Long>