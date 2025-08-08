package com.qinet.feastique.service.food

import com.qinet.feastique.model.entity.food.FoodSize
import com.qinet.feastique.repository.food.FoodSizeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodSizeService(
    private val foodSizeRepository: FoodSizeRepository
) {

    // No use-case for this yet.
    @Transactional(readOnly = true)
    fun getAllFoodSizes(foodId: Long): List<FoodSize> {
        return foodSizeRepository.findAllByFoodId(foodId)
    }

    fun getFoodSize(id: Long, foodId: Long): FoodSize? {
        return foodSizeRepository.findByIdAndFoodId(id, foodId)
    }


    // No use-case for this yet.
    @Transactional
    fun addFoodSize(foodSize: FoodSize): FoodSize {
        return foodSizeRepository.save(foodSize)
    }

    @Transactional
    fun deleteFoodSize(foodSize: FoodSize) {
        foodSizeRepository.delete(foodSize)
    }


    // No use-case for this yet
    @Transactional
    fun deleteFoodSize(id: Long, foodId: Long) {
        foodSizeRepository.deleteByIdAndFoodId(id, foodId)
    }

    @Transactional
    fun deleteAllFoodSizes(foodId: Long) {
        foodSizeRepository.deleteAllByFoodId(foodId)
    }
}

