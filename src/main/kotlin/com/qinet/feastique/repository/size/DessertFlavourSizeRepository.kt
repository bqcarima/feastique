package com.qinet.feastique.repository.size

import com.qinet.feastique.model.entity.size.DessertFlavourSize
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DessertFlavourSizeRepository : JpaRepository<DessertFlavourSize, UUID>

