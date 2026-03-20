package com.qinet.feastique.repository.size

import com.qinet.feastique.model.entity.size.FoodSize
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodSizeRepository : JpaRepository<FoodSize, UUID>