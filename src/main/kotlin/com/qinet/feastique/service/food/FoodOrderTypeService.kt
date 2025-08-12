package com.qinet.feastique.service.food

import com.qinet.feastique.model.entity.food.FoodOrderType
import com.qinet.feastique.repository.food.FoodOrderTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodOrderTypeService(
    private val foodOrderTypeRepository: FoodOrderTypeRepository
) {

    @Transactional(readOnly = true)
    fun getOrderType(id: Long, foodId: Long): FoodOrderType? {
        return foodOrderTypeRepository.findByIdAndFoodId(id, foodId)
    }

    // No use-case for this yet
    // Currently for test only
    @Transactional(readOnly = true)
    fun getAllOrderTypes(foodId: Long): List<FoodOrderType> {
        return foodOrderTypeRepository.findAllByFoodId(foodId)
    }

    @Transactional
    fun deleteFoodOrderType(foodOrderType: FoodOrderType) {
        foodOrderTypeRepository.delete(foodOrderType)
    }

    @Transactional
    fun deleteAllFoodOrderTypes(foodId: Long) {
        foodOrderTypeRepository.deleteAllByFoodId(foodId)
    }
}