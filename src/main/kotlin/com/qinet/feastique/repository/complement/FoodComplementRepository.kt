package com.qinet.feastique.repository.complement

import com.qinet.feastique.model.entity.complement.FoodComplement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FoodComplementRepository : JpaRepository<FoodComplement, UUID>

