package com.qinet.feastique.service.addOn

import com.qinet.feastique.model.entity.addOn.FoodAddOn
import com.qinet.feastique.repository.addOn.FoodAddOnRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class FoodAddOnService(
    private val foodAddOnRepository: FoodAddOnRepository
) {

    @Transactional(readOnly = true)
    fun getFoodAddOn(id: Long): Optional<FoodAddOn> {
        return foodAddOnRepository.findById(id)
    }

    @Transactional(readOnly = true)
    fun getAllFoodAddOns(foodId: Long): List<FoodAddOn> {
        return foodAddOnRepository.findAllByFoodId(foodId)
    }

    @Transactional
    fun saveFoodAddOn(foodAddOn: FoodAddOn) {
        foodAddOnRepository.save(foodAddOn)
    }

    @Transactional
    fun deleteFoodAddOn(foodAddOn: FoodAddOn) {
        foodAddOnRepository.delete(foodAddOn)
    }

    @Transactional
    fun deleteFoodAddOn(foodAddOnId: Long, foodId: Long) {
        foodAddOnRepository.deleteByAddOnIdAndFoodId(foodAddOnId, foodId)
    }

    @Transactional
    fun deleteAllFoodAddOnsByFoodId(foodId: Long) {
        foodAddOnRepository.deleteAllByFoodId(foodId)
    }

    @Transactional
    fun deleteAllFoodAddOnsByAddOnId(addOnId: Long) {
        foodAddOnRepository.deleteAllByAddOnId(addOnId)
    }
}