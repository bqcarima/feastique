package com.qinet.feastique.repository.order

import com.fasterxml.jackson.module.kotlin.ULongSerializer
import com.qinet.feastique.model.entity.addOn.FoodOrderAddOn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodOrderAddOnRepository : JpaRepository<FoodOrderAddOn, ULongSerializer> {
}