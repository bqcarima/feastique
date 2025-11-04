package com.qinet.feastique.repository.food

import com.qinet.feastique.model.entity.provisions.food.FoodImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodImageRepository : JpaRepository<FoodImage, UUID>